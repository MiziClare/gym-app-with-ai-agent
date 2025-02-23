package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.mapper")
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
        System.out.println("\n================================ Hello =================================");
        System.out.println("|                                                                       |");
        System.out.println("| \t\t\t\t\t\t\t\t  Test  \t\t\t\t\t\t\t\t|");
        System.out.println("| \t\t\t\t\t\t\t\t Success\t\t\t\t\t\t\t\t|");
        System.out.println("|                                                                       |");
        System.out.println("================================ World =================================");
    }

}
