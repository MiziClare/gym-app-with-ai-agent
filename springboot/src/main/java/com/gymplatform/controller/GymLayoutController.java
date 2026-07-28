package com.gymplatform.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.*;

@RestController
@RequestMapping("/api/admin/gym-layout")
public class GymLayoutController {
    private final JdbcTemplate jdbc;

    public GymLayoutController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    LayoutResponse getLayout() {
        var spaces = jdbc.query("""
                SELECT space.id, space.floor_id, space.name, space.type,
                       space.x_percent, space.y_percent,
                       space.width_percent, space.height_percent,
                       COUNT(DISTINCT equipment.id) AS equipment_count,
                       COUNT(DISTINCT session.id) AS session_count
                FROM gym_spaces space
                LEFT JOIN equipment ON equipment.space_id = space.id
                LEFT JOIN course_sessions session ON session.space_id = space.id
                GROUP BY space.id, space.floor_id, space.name, space.type,
                         space.x_percent, space.y_percent,
                         space.width_percent, space.height_percent
                ORDER BY space.type, space.id
                """, (result, row) -> new SpaceResponse(
                result.getLong("id"),
                result.getLong("floor_id"),
                result.getString("name"),
                result.getString("type"),
                result.getBigDecimal("x_percent"),
                result.getBigDecimal("y_percent"),
                result.getBigDecimal("width_percent"),
                result.getBigDecimal("height_percent"),
                result.getInt("equipment_count"),
                result.getInt("session_count")
        ));
        var byFloor = new HashMap<Long, List<SpaceResponse>>();
        spaces.forEach(space -> byFloor.computeIfAbsent(space.floorId(), ignored -> new ArrayList<>()).add(space));
        var floors = jdbc.query("""
                SELECT id, name, sort_order FROM gym_floors ORDER BY sort_order
                """, (result, row) -> new FloorResponse(
                result.getLong("id"),
                result.getString("name"),
                result.getInt("sort_order"),
                byFloor.getOrDefault(result.getLong("id"), List.of())
        ));
        return new LayoutResponse(floors);
    }

    @PutMapping
    @Transactional
    LayoutResponse saveLayout(@Valid @RequestBody LayoutRequest request) {
        validateLayout(request);

        var existingFloorIds = new HashSet<>(jdbc.queryForList("SELECT id FROM gym_floors", Long.class));
        var requestedFloorIds = request.floors().stream()
                .map(FloorRequest::id).filter(Objects::nonNull).toList();
        if (new HashSet<>(requestedFloorIds).size() != requestedFloorIds.size()
                || !existingFloorIds.containsAll(requestedFloorIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown or repeated floor id");
        }
        var existingSpaceFloors = new HashMap<Long, Long>();
        jdbc.query("SELECT id, floor_id FROM gym_spaces", result -> {
            existingSpaceFloors.put(result.getLong("id"), result.getLong("floor_id"));
        });
        var requestedSpaceIds = new HashSet<Long>();
        for (var floor : request.floors()) {
            for (var space : floor.spaces()) {
                if (space.id() == null) continue;
                if (!Objects.equals(existingSpaceFloors.get(space.id()), floor.id())
                        || !requestedSpaceIds.add(space.id())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown or repeated space id");
                }
            }
        }
        requireUnlinkedDeletions(existingSpaceFloors.keySet(), requestedSpaceIds);

        jdbc.update("UPDATE gym_floors SET name = CONCAT('__layout_', id), sort_order = sort_order + 100");
        jdbc.update("UPDATE gym_spaces SET name = CONCAT('__layout_', id)");

        var keptFloorIds = new HashSet<Long>();
        var keptSpaceIds = new HashSet<Long>();
        for (int floorIndex = 0; floorIndex < request.floors().size(); floorIndex++) {
            var floor = request.floors().get(floorIndex);
            var floorId = floor.id() == null
                    ? insertFloor(floor.name().trim(), floorIndex)
                    : floor.id();
            if (floor.id() != null) {
                jdbc.update("UPDATE gym_floors SET name = ?, sort_order = ? WHERE id = ?",
                        floor.name().trim(), floorIndex, floorId);
            }
            keptFloorIds.add(floorId);

            for (var space : floor.spaces()) {
                var spaceId = space.id() == null ? insertSpace(floorId, space) : space.id();
                if (space.id() != null) {
                    jdbc.update("""
                            UPDATE gym_spaces
                            SET name = ?, type = ?, x_percent = ?, y_percent = ?,
                                width_percent = ?, height_percent = ?
                            WHERE id = ?
                            """, space.name().trim(), space.type(), space.x(), space.y(),
                            space.width(), space.height(), spaceId);
                }
                keptSpaceIds.add(spaceId);
            }
        }

        existingSpaceFloors.keySet().stream()
                .filter(id -> !keptSpaceIds.contains(id))
                .forEach(id -> jdbc.update("DELETE FROM gym_spaces WHERE id = ?", id));
        existingFloorIds.stream()
                .filter(id -> !keptFloorIds.contains(id))
                .forEach(id -> jdbc.update("DELETE FROM gym_floors WHERE id = ?", id));
        return getLayout();
    }

    void requireUnlinkedDeletions(Set<Long> existingSpaceIds, Set<Long> keptSpaceIds) {
        for (var id : existingSpaceIds) {
            if (keptSpaceIds.contains(id)) continue;
            var links = jdbc.queryForObject("""
                    SELECT (
                        SELECT COUNT(*) FROM equipment WHERE space_id = ?
                    ) + (
                        SELECT COUNT(*) FROM course_sessions WHERE space_id = ?
                    )
                    """, Integer.class, id, id);
            if (links != null && links > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Move linked equipment and class sessions before deleting this space"
                );
            }
        }
    }

    static void validateLayout(LayoutRequest request) {
        if (request.floors() == null || request.floors().isEmpty() || request.floors().size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Layout must contain 1 to 20 floors");
        }
        var floorNames = new HashSet<String>();
        for (var floor : request.floors()) {
            if (floor.name() == null || floor.name().isBlank() || floor.name().trim().length() > 80) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Floor name is required");
            }
            if (!floorNames.add(floor.name().trim().toLowerCase(Locale.ROOT))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Floor names must be unique");
            }
            if (floor.spaces() == null || floor.spaces().size() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A floor can contain at most 100 spaces");
            }
            var spaceNames = new HashSet<String>();
            var spaceIds = new HashSet<Long>();
            for (var space : floor.spaces()) {
                if (space.name() == null || space.name().isBlank() || space.name().trim().length() > 80
                        || !Set.of("ROOM", "AREA").contains(space.type())
                        || !spaceNames.add(space.name().trim().toLowerCase(Locale.ROOT))
                        || space.id() != null && !spaceIds.add(space.id())
                        || !validGeometry(space)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or repeated space");
                }
            }
        }
    }

    private static boolean validGeometry(SpaceRequest space) {
        if (space.x() == null || space.y() == null || space.width() == null || space.height() == null) return false;
        var zero = BigDecimal.ZERO;
        var hundred = BigDecimal.valueOf(100);
        var minimum = BigDecimal.valueOf(3);
        return space.x().compareTo(zero) >= 0 && space.y().compareTo(zero) >= 0
                && space.width().compareTo(minimum) >= 0 && space.height().compareTo(minimum) >= 0
                && space.x().add(space.width()).compareTo(hundred) <= 0
                && space.y().add(space.height()).compareTo(hundred) <= 0;
    }

    private Long insertFloor(String name, int sortOrder) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO gym_floors (name, sort_order) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setInt(2, sortOrder);
            return statement;
        }, keys);
        return Objects.requireNonNull(keys.getKey()).longValue();
    }

    private Long insertSpace(Long floorId, SpaceRequest space) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO gym_spaces
                        (floor_id, name, type, x_percent, y_percent, width_percent, height_percent)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, floorId);
            statement.setString(2, space.name().trim());
            statement.setString(3, space.type());
            statement.setBigDecimal(4, space.x());
            statement.setBigDecimal(5, space.y());
            statement.setBigDecimal(6, space.width());
            statement.setBigDecimal(7, space.height());
            return statement;
        }, keys);
        return Objects.requireNonNull(keys.getKey()).longValue();
    }

    public record LayoutRequest(
            @NotNull @Size(min = 1, max = 20) List<@Valid FloorRequest> floors
    ) {}

    public record FloorRequest(
            Long id,
            @NotBlank @Size(max = 80) String name,
            @NotNull @Size(max = 100) List<@Valid SpaceRequest> spaces
    ) {}

    public record SpaceRequest(
            Long id,
            @NotBlank @Size(max = 80) String name,
            @NotBlank String type,
            @NotNull BigDecimal x,
            @NotNull BigDecimal y,
            @NotNull BigDecimal width,
            @NotNull BigDecimal height
    ) {}

    public record LayoutResponse(List<FloorResponse> floors) {}

    public record FloorResponse(Long id, String name, int sortOrder, List<SpaceResponse> spaces) {}

    public record SpaceResponse(
            Long id,
            Long floorId,
            String name,
            String type,
            BigDecimal x,
            BigDecimal y,
            BigDecimal width,
            BigDecimal height,
            int equipmentCount,
            int sessionCount
    ) {}
}
