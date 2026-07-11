package com.cmh.agrimarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 产地信息公示：记录产地溯源、农户与资质信息。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "origin")
public class Origin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 产地/基地名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 产地地址（省/市/县/村） */
    @Column(length = 200)
    private String location;

    /** 农户或合作社名称 */
    @Column(length = 100)
    private String farmer;

    /** 联系电话 */
    @Column(length = 30)
    private String phone;

    /** 资质/认证（如：绿色食品、有机认证） */
    @Column(length = 200)
    private String certificate;

    /** 产地/种植养殖介绍 */
    @Column(length = 1000)
    private String description;
}
