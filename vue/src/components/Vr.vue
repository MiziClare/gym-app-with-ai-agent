<template>
    <div class="vr-container">
        <!-- VR icon button -->
        <div class="vr-icon" @click="toggleVr" v-if="true">
            <img src="../assets/imgs/icon-vr.png" alt="VR Experience" />
        </div>

        <!-- VR window -->
        <div class="vr-window" v-if="vrVisible">
            <div class="vr-header">
                <span>Gym Tour</span>
                <span class="close-btn" @click="toggleVr">&times;</span>
            </div>

            <div class="main-container" v-if="vrVisible">
                <div id="vr-scene-container"></div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Vr',
    data() {
        return {
            vrVisible: false,
            scene: null
        };
    },
    methods: {
        toggleVr() {
            this.vrVisible = !this.vrVisible;

            if (this.vrVisible) {
                // delay initialization of the scene to ensure the DOM has been rendered
                this.$nextTick(() => {
                    this.initScene();
                });
            } else {
                // clean up the scene resources
                this.cleanupScene();
            }
        },

        initScene() {
            const container = document.getElementById('vr-scene-container');
            if (!container) return;

            // create the scene element
            this.scene = document.createElement('a-scene');
            this.scene.setAttribute('embedded', '');

            // add the assets area
            const assets = document.createElement('a-assets');
            this.scene.appendChild(assets);

            // add the camera
            const cameraEntity = document.createElement('a-entity');
            cameraEntity.setAttribute('position', '0 1.6 8');

            const camera = document.createElement('a-camera');
            camera.setAttribute('wasd-controls', 'acceleration: 30');
            camera.setAttribute('look-controls', '');

            const raycaster = document.createElement('a-entity');
            raycaster.setAttribute('raycaster', 'objects: .collidable; far: 1.5; direction: 0 0 -1');
            raycaster.setAttribute('collision-handler', '');

            camera.appendChild(raycaster);
            cameraEntity.appendChild(camera);
            this.scene.appendChild(cameraEntity);

            // add the ground
            const ground = document.createElement('a-plane');
            ground.setAttribute('rotation', '-90 0 0');
            ground.setAttribute('width', '50');
            ground.setAttribute('height', '50');
            ground.setAttribute('color', '#7BC8A4');
            this.scene.appendChild(ground);

            // add the four walls
            // back wall
            const backWall = document.createElement('a-box');
            backWall.setAttribute('position', '0 2.5 -25');
            backWall.setAttribute('width', '50');
            backWall.setAttribute('height', '5');
            backWall.setAttribute('depth', '1');
            backWall.setAttribute('color', '#CCCCCC');
            backWall.setAttribute('class', 'collidable');
            this.scene.appendChild(backWall);

            // left wall
            const leftWall = document.createElement('a-box');
            leftWall.setAttribute('position', '-25 2.5 0');
            leftWall.setAttribute('width', '1');
            leftWall.setAttribute('height', '5');
            leftWall.setAttribute('depth', '50');
            leftWall.setAttribute('color', '#CCCCCC');
            leftWall.setAttribute('class', 'collidable');
            this.scene.appendChild(leftWall);

            // right wall
            const rightWall = document.createElement('a-box');
            rightWall.setAttribute('position', '25 2.5 0');
            rightWall.setAttribute('width', '1');
            rightWall.setAttribute('height', '5');
            rightWall.setAttribute('depth', '50');
            rightWall.setAttribute('color', '#CCCCCC');
            rightWall.setAttribute('class', 'collidable');
            this.scene.appendChild(rightWall);

            // front wall
            const frontWall = document.createElement('a-box');
            frontWall.setAttribute('position', '0 2.5 25');
            frontWall.setAttribute('width', '50');
            frontWall.setAttribute('height', '5');
            frontWall.setAttribute('depth', '1');
            frontWall.setAttribute('color', '#CCCCCC');
            frontWall.setAttribute('class', 'collidable');
            this.scene.appendChild(frontWall);

            // ceiling
            const ceiling = document.createElement('a-box');
            ceiling.setAttribute('position', '0 5 0');
            ceiling.setAttribute('width', '50');
            ceiling.setAttribute('height', '1');
            ceiling.setAttribute('depth', '50');
            ceiling.setAttribute('color', '#EEEEEE');
            this.scene.appendChild(ceiling);

            // lights
            const ambientLight = document.createElement('a-entity');
            ambientLight.setAttribute('light', 'type: ambient; intensity: 0.4');
            this.scene.appendChild(ambientLight);

            const directionalLight = document.createElement('a-entity');
            directionalLight.setAttribute('light', 'type: directional; intensity: 0.8');
            directionalLight.setAttribute('position', '0 10 0');
            this.scene.appendChild(directionalLight);

            /* ================================
               Add the code for each area below
               ================================ */

            // 1. Swimming pool shell
            const swimmingPoolShell = document.createElement('a-box');
            swimmingPoolShell.setAttribute('position', '10 0 10');
            swimmingPoolShell.setAttribute('width', '15');
            swimmingPoolShell.setAttribute('height', '0.5');
            swimmingPoolShell.setAttribute('depth', '10');
            swimmingPoolShell.setAttribute('color', '#E0E0E0');
            swimmingPoolShell.setAttribute('class', 'collidable');

            // inner water surface
            const waterSurface = document.createElement('a-box');
            waterSurface.setAttribute('position', '0 0.3 0');
            waterSurface.setAttribute('width', '14');
            waterSurface.setAttribute('height', '0.1');
            waterSurface.setAttribute('depth', '9');
            waterSurface.setAttribute('color', '#64B5F6');
            waterSurface.setAttribute('opacity', '0.8');
            // water surface ripple animation
            const waterAnimation = document.createElement('a-animation');
            waterAnimation.setAttribute('attribute', 'material.opacity');
            waterAnimation.setAttribute('from', '0.7');
            waterAnimation.setAttribute('to', '0.9');
            waterAnimation.setAttribute('dur', '2000');
            waterAnimation.setAttribute('repeat', 'indefinite');
            waterAnimation.setAttribute('direction', 'alternate');
            waterSurface.appendChild(waterAnimation);
            swimmingPoolShell.appendChild(waterSurface);

            // swimming pool stairs
            ['-6 0.3 0', '-5 0.2 0', '-4 0.1 0'].forEach(pos => {
                const step = document.createElement('a-box');
                step.setAttribute('position', pos);
                step.setAttribute('width', '2');
                step.setAttribute('height', '0.1');
                step.setAttribute('depth', '9');
                step.setAttribute('color', '#90CAF9');
                step.setAttribute('opacity', '0.9');
                step.setAttribute('class', 'collidable');
                swimmingPoolShell.appendChild(step);
            });

            // swimming pool lane lines
            const laneLinesContainer = document.createElement('a-entity');
            laneLinesContainer.setAttribute('position', '0 0.32 0');
            ['-3', '-1.5', '0', '1.5', '3'].forEach(z => {
                const lane = document.createElement('a-box');
                lane.setAttribute('position', `0 0 ${z}`);
                lane.setAttribute('width', '14');
                lane.setAttribute('height', '0.01');
                lane.setAttribute('depth', '0.1');
                lane.setAttribute('color', '#FFFFFF');
                laneLinesContainer.appendChild(lane);
            });
            swimmingPoolShell.appendChild(laneLinesContainer);
            this.scene.appendChild(swimmingPoolShell);

            // 2. decoration: lifebuoy and lifebuoy chair
            const lifebuoy = document.createElement('a-torus');
            lifebuoy.setAttribute('position', '17 0.7 10');
            lifebuoy.setAttribute('radius', '0.5');
            lifebuoy.setAttribute('radius-tubular', '0.1');
            lifebuoy.setAttribute('color', '#FF5252');
            lifebuoy.setAttribute('class', 'collidable');
            this.scene.appendChild(lifebuoy);

            const lifebuoyChair = document.createElement('a-box');
            lifebuoyChair.setAttribute('position', '17 1 13');
            lifebuoyChair.setAttribute('width', '1');
            lifebuoyChair.setAttribute('height', '2');
            lifebuoyChair.setAttribute('depth', '1');
            lifebuoyChair.setAttribute('color', '#8D6E63');
            lifebuoyChair.setAttribute('class', 'collidable');
            this.scene.appendChild(lifebuoyChair);

            // 3. classroom area
            // classroom 1
            const classroom1 = document.createElement('a-box');
            classroom1.setAttribute('position', '-10 1.5 -10');
            classroom1.setAttribute('width', '8');
            classroom1.setAttribute('height', '3');
            classroom1.setAttribute('depth', '8');
            classroom1.setAttribute('color', '#FF8C00');
            classroom1.setAttribute('opacity', '0.7');
            classroom1.setAttribute('class', 'collidable');
            const classroom1Text = document.createElement('a-text');
            classroom1Text.setAttribute('value', 'classroom 1');
            classroom1Text.setAttribute('align', 'center');
            classroom1Text.setAttribute('position', '0 2 0');
            classroom1Text.setAttribute('color', '#FFF');
            classroom1.appendChild(classroom1Text);
            this.scene.appendChild(classroom1);

            // classroom 2
            const classroom2 = document.createElement('a-box');
            classroom2.setAttribute('position', '10 1.5 -10');
            classroom2.setAttribute('width', '8');
            classroom2.setAttribute('height', '3');
            classroom2.setAttribute('depth', '8');
            classroom2.setAttribute('color', '#FFD700');
            classroom2.setAttribute('opacity', '0.7');
            classroom2.setAttribute('class', 'collidable');
            const classroom2Text = document.createElement('a-text');
            classroom2Text.setAttribute('value', 'classroom 2');
            classroom2Text.setAttribute('align', 'center');
            classroom2Text.setAttribute('position', '0 2 0');
            classroom2Text.setAttribute('color', '#000');
            classroom2.appendChild(classroom2Text);
            this.scene.appendChild(classroom2);

            // classroom 3
            const classroom3 = document.createElement('a-box');
            classroom3.setAttribute('position', '-10 1.5 0');
            classroom3.setAttribute('width', '8');
            classroom3.setAttribute('height', '3');
            classroom3.setAttribute('depth', '8');
            classroom3.setAttribute('color', '#ADFF2F');
            classroom3.setAttribute('opacity', '0.7');
            classroom3.setAttribute('class', 'collidable');
            const classroom3Text = document.createElement('a-text');
            classroom3Text.setAttribute('value', 'classroom 3');
            classroom3Text.setAttribute('align', 'center');
            classroom3Text.setAttribute('position', '0 2 0');
            classroom3Text.setAttribute('color', '#000');
            classroom3.appendChild(classroom3Text);
            this.scene.appendChild(classroom3);

            // 4. equipment area (area identifier)
            const equipmentArea = document.createElement('a-box');
            equipmentArea.setAttribute('position', '-15 1.5 15');
            equipmentArea.setAttribute('width', '0.5');
            equipmentArea.setAttribute('height', '3');
            equipmentArea.setAttribute('depth', '0.5');
            equipmentArea.setAttribute('color', '#333333');
            equipmentArea.setAttribute('class', 'collidable');
            const equipmentAreaText = document.createElement('a-text');
            equipmentAreaText.setAttribute('value', 'equipment area');
            equipmentAreaText.setAttribute('align', 'center');
            equipmentAreaText.setAttribute('position', '0 1.6 0');
            equipmentAreaText.setAttribute('rotation', '0 45 0');
            equipmentAreaText.setAttribute('color', '#FFFFFF');
            equipmentAreaText.setAttribute('width', '10');
            equipmentArea.appendChild(equipmentAreaText);
            this.scene.appendChild(equipmentArea);

            // 5. treadmill area: 5 treadmills evenly arranged
            const treadmillArea = document.createElement('a-entity');
            treadmillArea.setAttribute('position', '-15 0 15');
            const treadmillAreaText = document.createElement('a-text');
            treadmillAreaText.setAttribute('value', 'treadmill area');
            treadmillAreaText.setAttribute('align', 'center');
            treadmillAreaText.setAttribute('position', '0 2 0');
            treadmillAreaText.setAttribute('color', '#000000');
            treadmillAreaText.setAttribute('width', '10');
            treadmillArea.appendChild(treadmillAreaText);

            const createTreadmill = (pos) => {
                const treadmill = document.createElement('a-entity');
                treadmill.setAttribute('class', 'treadmill');
                treadmill.setAttribute('position', pos);
                treadmill.setAttribute('rotation', '0 90 0');

                const base = document.createElement('a-box');
                base.setAttribute('width', '2');
                base.setAttribute('height', '0.1');
                base.setAttribute('depth', '1');
                base.setAttribute('color', '#333333');
                base.setAttribute('class', 'collidable');
                treadmill.appendChild(base);

                const belt = document.createElement('a-box');
                belt.setAttribute('position', '0 0.15 0');
                belt.setAttribute('width', '1.8');
                belt.setAttribute('height', '0.05');
                belt.setAttribute('depth', '0.8');
                belt.setAttribute('color', '#111111');
                belt.setAttribute('class', 'collidable');
                treadmill.appendChild(belt);

                const support = document.createElement('a-box');
                support.setAttribute('position', '0.8 0.6 0');
                support.setAttribute('width', '0.2');
                support.setAttribute('height', '1');
                support.setAttribute('depth', '0.8');
                support.setAttribute('color', '#333333');
                support.setAttribute('class', 'collidable');
                treadmill.appendChild(support);

                const top = document.createElement('a-box');
                top.setAttribute('position', '0.8 1.2 0');
                top.setAttribute('width', '0.8');
                top.setAttribute('height', '0.2');
                top.setAttribute('depth', '0.8');
                top.setAttribute('color', '#333333');
                top.setAttribute('class', 'collidable');
                treadmill.appendChild(top);

                const indicator = document.createElement('a-cylinder');
                indicator.setAttribute('position', '0.8 1.3 -0.3');
                indicator.setAttribute('radius', '0.05');
                indicator.setAttribute('height', '0.1');
                indicator.setAttribute('color', '#FF0000');
                indicator.setAttribute('rotation', '90 0 0');
                treadmill.appendChild(indicator);

                return treadmill;
            };

            ['-8 0 0', '-4 0 0', '0 0 0', '4 0 0', '8 0 0'].forEach(pos => {
                const treadmill = createTreadmill(pos);
                treadmillArea.appendChild(treadmill);
            });
            this.scene.appendChild(treadmillArea);

            // 6. elliptical machine area: 5 elliptical machines evenly arranged
            const ellipticalArea = document.createElement('a-entity');
            ellipticalArea.setAttribute('position', '-15 0 5');
            const ellipticalAreaText = document.createElement('a-text');
            ellipticalAreaText.setAttribute('value', 'elliptical machine area');
            ellipticalAreaText.setAttribute('align', 'center');
            ellipticalAreaText.setAttribute('position', '0 2 0');
            ellipticalAreaText.setAttribute('color', '#000000');
            ellipticalAreaText.setAttribute('width', '10');
            ellipticalArea.appendChild(ellipticalAreaText);

            const createElliptical = (pos) => {
                const elliptical = document.createElement('a-entity');
                elliptical.setAttribute('class', 'elliptical');
                elliptical.setAttribute('position', pos);
                elliptical.setAttribute('rotation', '0 90 0');

                const base = document.createElement('a-box');
                base.setAttribute('width', '1.2');
                base.setAttribute('height', '0.1');
                base.setAttribute('depth', '0.6');
                base.setAttribute('color', '#444444');
                base.setAttribute('class', 'collidable');
                elliptical.appendChild(base);

                const support = document.createElement('a-box');
                support.setAttribute('position', '0 0.6 0');
                support.setAttribute('width', '0.1');
                support.setAttribute('height', '1');
                support.setAttribute('depth', '0.1');
                support.setAttribute('color', '#444444');
                support.setAttribute('class', 'collidable');
                elliptical.appendChild(support);

                const top = document.createElement('a-box');
                top.setAttribute('position', '0 1.2 0');
                top.setAttribute('width', '0.6');
                top.setAttribute('height', '0.1');
                top.setAttribute('depth', '0.4');
                top.setAttribute('color', '#444444');
                top.setAttribute('class', 'collidable');
                elliptical.appendChild(top);

                const torusKnot1 = document.createElement('a-torus-knot');
                torusKnot1.setAttribute('position', '0.4 0.5 0.2');
                torusKnot1.setAttribute('radius', '0.1');
                torusKnot1.setAttribute('radius-tubular', '0.01');
                torusKnot1.setAttribute('p', '2');
                torusKnot1.setAttribute('q', '3');
                torusKnot1.setAttribute('color', '#666666');
                elliptical.appendChild(torusKnot1);

                const torusKnot2 = document.createElement('a-torus-knot');
                torusKnot2.setAttribute('position', '0.4 0.5 -0.2');
                torusKnot2.setAttribute('radius', '0.1');
                torusKnot2.setAttribute('radius-tubular', '0.01');
                torusKnot2.setAttribute('p', '2');
                torusKnot2.setAttribute('q', '3');
                torusKnot2.setAttribute('color', '#666666');
                elliptical.appendChild(torusKnot2);

                const cylinder = document.createElement('a-cylinder');
                cylinder.setAttribute('position', '0 1.5 0');
                cylinder.setAttribute('radius', '0.05');
                cylinder.setAttribute('height', '0.6');
                cylinder.setAttribute('color', '#444444');
                cylinder.setAttribute('rotation', '0 0 90');
                elliptical.appendChild(cylinder);

                return elliptical;
            };

            ['-8 0 0', '-4 0 0', '0 0 0', '4 0 0', '8 0 0'].forEach(pos => {
                const elliptical = createElliptical(pos);
                ellipticalArea.appendChild(elliptical);
            });
            this.scene.appendChild(ellipticalArea);

            // 7. dumbbell area: 5 dumbbell sets evenly arranged
            const dumbbellArea = document.createElement('a-entity');
            dumbbellArea.setAttribute('position', '-15 0 -5');
            const dumbbellAreaText = document.createElement('a-text');
            dumbbellAreaText.setAttribute('value', 'dumbbell area');
            dumbbellAreaText.setAttribute('align', 'center');
            dumbbellAreaText.setAttribute('position', '0 2 0');
            dumbbellAreaText.setAttribute('color', '#000000');
            dumbbellAreaText.setAttribute('width', '10');
            dumbbellArea.appendChild(dumbbellAreaText);

            // dumbbell rack
            const dumbbellRack = document.createElement('a-box');
            dumbbellRack.setAttribute('position', '0 0.75 0');
            dumbbellRack.setAttribute('width', '18');
            dumbbellRack.setAttribute('height', '1.5');
            dumbbellRack.setAttribute('depth', '1');
            dumbbellRack.setAttribute('color', '#8B4513');
            dumbbellRack.setAttribute('class', 'collidable');
            dumbbellArea.appendChild(dumbbellRack);

            const createDumbbellSet = (pos, color, sphereRadius) => {
                const dumbbellSet = document.createElement('a-entity');
                dumbbellSet.setAttribute('class', 'dumbbell-set');
                dumbbellSet.setAttribute('position', pos);

                const cylinder = document.createElement('a-cylinder');
                cylinder.setAttribute('position', '0 0 0');
                cylinder.setAttribute('radius', '0.1');
                cylinder.setAttribute('height', '0.6');
                cylinder.setAttribute('color', color);
                cylinder.setAttribute('rotation', '0 0 90');
                cylinder.setAttribute('class', 'collidable');
                dumbbellSet.appendChild(cylinder);

                const sphere1 = document.createElement('a-sphere');
                sphere1.setAttribute('position', '-0.35 0 0');
                sphere1.setAttribute('radius', sphereRadius);
                sphere1.setAttribute('color', color);
                sphere1.setAttribute('class', 'collidable');
                dumbbellSet.appendChild(sphere1);

                const sphere2 = document.createElement('a-sphere');
                sphere2.setAttribute('position', '0.35 0 0');
                sphere2.setAttribute('radius', sphereRadius);
                sphere2.setAttribute('color', color);
                sphere2.setAttribute('class', 'collidable');
                dumbbellSet.appendChild(sphere2);

                return dumbbellSet;
            };

            const dumbbellSets = [
                { pos: '-8 1.1 0', color: '#4CC3D9', sphereRadius: '0.2' },
                { pos: '-4 1.1 0', color: '#EF2D5E', sphereRadius: '0.25' },
                { pos: '0 1.1 0', color: '#FFC65D', sphereRadius: '0.3' },
                { pos: '4 1.1 0', color: '#7BC8A4', sphereRadius: '0.35' },
                { pos: '8 1.1 0', color: '#333333', sphereRadius: '0.4' }
            ];

            dumbbellSets.forEach(set => {
                const dumbbellSet = createDumbbellSet(set.pos, set.color, set.sphereRadius);
                dumbbellArea.appendChild(dumbbellSet);
            });
            this.scene.appendChild(dumbbellArea);

            // 8. private coach area
            const privateCoachArea = document.createElement('a-entity');
            privateCoachArea.setAttribute('position', '0 0 -15');
            const privateCoachAreaText = document.createElement('a-text');
            privateCoachAreaText.setAttribute('value', 'private coach area');
            privateCoachAreaText.setAttribute('align', 'center');
            privateCoachAreaText.setAttribute('position', '0 2 0');
            privateCoachAreaText.setAttribute('color', '#000000');
            privateCoachAreaText.setAttribute('width', '10');
            privateCoachArea.appendChild(privateCoachAreaText);

            // private coach platform
            const privateCoachPlatform = document.createElement('a-box');
            privateCoachPlatform.setAttribute('position', '0 0.3 0');
            privateCoachPlatform.setAttribute('width', '10');
            privateCoachPlatform.setAttribute('height', '0.6');
            privateCoachPlatform.setAttribute('depth', '5');
            privateCoachPlatform.setAttribute('color', '#8B4513');
            privateCoachPlatform.setAttribute('class', 'collidable');
            const privateCoachPlatformInner = document.createElement('a-box');
            privateCoachPlatformInner.setAttribute('position', '0 0.4 0');
            privateCoachPlatformInner.setAttribute('width', '9');
            privateCoachPlatformInner.setAttribute('height', '0.2');
            privateCoachPlatformInner.setAttribute('depth', '4');
            privateCoachPlatformInner.setAttribute('color', '#A0522D');
            privateCoachPlatformInner.setAttribute('class', 'collidable');
            privateCoachPlatform.appendChild(privateCoachPlatformInner);
            privateCoachArea.appendChild(privateCoachPlatform);

            // fitness equipment in the private coach area
            // 1. fitness ball
            const fitnessBall = document.createElement('a-sphere');
            fitnessBall.setAttribute('position', '-4 1 0');
            fitnessBall.setAttribute('radius', '0.8');
            fitnessBall.setAttribute('color', '#FF5252');
            fitnessBall.setAttribute('class', 'collidable');
            privateCoachArea.appendChild(fitnessBall);
            // 2. yoga mat
            const yogaMat = document.createElement('a-box');
            yogaMat.setAttribute('position', '-2 0.65 0');
            yogaMat.setAttribute('width', '1.5');
            yogaMat.setAttribute('height', '0.05');
            yogaMat.setAttribute('depth', '3');
            yogaMat.setAttribute('color', '#BA68C8');
            yogaMat.setAttribute('class', 'collidable');
            privateCoachArea.appendChild(yogaMat);
            // 3. elastic band
            const elasticBand = document.createElement('a-torus');
            elasticBand.setAttribute('position', '0 1 0');
            elasticBand.setAttribute('radius', '0.4');
            elasticBand.setAttribute('radius-tubular', '0.05');
            elasticBand.setAttribute('color', '#64B5F6');
            elasticBand.setAttribute('class', 'collidable');
            privateCoachArea.appendChild(elasticBand);
            // 4. balance board
            const balanceBoard = document.createElement('a-cylinder');
            balanceBoard.setAttribute('position', '2 0.7 0');
            balanceBoard.setAttribute('radius', '0.8');
            balanceBoard.setAttribute('height', '0.1');
            balanceBoard.setAttribute('color', '#4CAF50');
            balanceBoard.setAttribute('class', 'collidable');
            const balanceBoardSphere = document.createElement('a-sphere');
            balanceBoardSphere.setAttribute('position', '0 -0.1 0');
            balanceBoardSphere.setAttribute('radius', '0.2');
            balanceBoardSphere.setAttribute('color', '#2E7D32');
            balanceBoardSphere.setAttribute('class', 'collidable');
            balanceBoard.appendChild(balanceBoardSphere);
            privateCoachArea.appendChild(balanceBoard);
            // 5. kettlebell
            const kettlebell = document.createElement('a-entity');
            kettlebell.setAttribute('position', '4 0.8 0');
            kettlebell.setAttribute('class', 'collidable');
            const kettlebellSphere = document.createElement('a-sphere');
            kettlebellSphere.setAttribute('position', '0 0 0');
            kettlebellSphere.setAttribute('radius', '0.3');
            kettlebellSphere.setAttribute('color', '#FFC107');
            kettlebellSphere.setAttribute('class', 'collidable');
            kettlebell.appendChild(kettlebellSphere);
            const kettlebellCylinder = document.createElement('a-cylinder');
            kettlebellCylinder.setAttribute('position', '0 0.4 0');
            kettlebellCylinder.setAttribute('radius', '0.1');
            kettlebellCylinder.setAttribute('height', '0.5');
            kettlebellCylinder.setAttribute('color', '#FFC107');
            kettlebellCylinder.setAttribute('class', 'collidable');
            kettlebell.appendChild(kettlebellCylinder);
            const kettlebellBox = document.createElement('a-box');
            kettlebellBox.setAttribute('position', '0 0.7 0');
            kettlebellBox.setAttribute('width', '0.4');
            kettlebellBox.setAttribute('height', '0.2');
            kettlebellBox.setAttribute('depth', '0.2');
            kettlebellBox.setAttribute('color', '#FFC107');
            kettlebellBox.setAttribute('class', 'collidable');
            kettlebell.appendChild(kettlebellBox);
            privateCoachArea.appendChild(kettlebell);
            this.scene.appendChild(privateCoachArea);

            // add the scene to the container
            container.appendChild(this.scene);

            // register the collision handler component
            if (!AFRAME.components['collision-handler']) {
                AFRAME.registerComponent('collision-handler', {
                    init: function () {
                        this.el.addEventListener('raycaster-intersection', function () {
                            // when the raycaster intersects with the object, stop the camera movement
                            const cameraEl = document.querySelector('[camera]').parentElement;
                            const currentPosition = cameraEl.getAttribute('position');
                            cameraEl.setAttribute('position', currentPosition);
                        });
                    }
                });
            }
        },

        cleanupScene() {
            if (this.scene) {
                const container = document.getElementById('vr-scene-container');
                if (container && container.contains(this.scene)) {
                    container.removeChild(this.scene);
                }
                this.scene = null;
            }
        }
    },
    beforeDestroy() {
        // clean up the resources before the component is destroyed
        this.cleanupScene();
    }
}
</script>

<style scoped>
.vr-container {
    position: fixed;
    right: 20px;
    /* located above the AI chat icon */
    bottom: 100px;
    z-index: 1000;
}

.vr-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #ffeaa7;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.vr-icon:hover {
    transform: scale(1.05);
}

.vr-icon img {
    width: 35px;
    height: 25px;
}

.vr-window {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80%;
    height: 80%;
    border-radius: 10px;
    background-color: #fff;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    z-index: 1001;
}

.vr-header {
    background-color: #ffeaa7;
    color: black;
    padding: 10px 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-family: 'Poppins', sans-serif;
}

.close-btn {
    font-size: 24px;
    cursor: pointer;
}

.main-container {
    flex: 1;
    width: 100%;
    height: calc(100% - 45px);
    border-radius: 0 0 15px 15px;
    overflow: hidden;
}

#vr-scene-container {
    width: 100%;
    height: 100%;
}
</style>