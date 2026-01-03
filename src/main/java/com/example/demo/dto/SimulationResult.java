package com.example.demo.dto;

import lombok.Data;

/**
 * シミュレーション結果
 */
@Data
public class SimulationResult {
    /** 入力データ */
    private SimulationInput input;
    
    /** 毎月の返済額（円） */
    private Long monthlyPayment;
    
    /** 返済額と現在の家賃の差額（円） */
    private Long rentDifference;
    
    /** 大手HMとの光熱費の差額（円/月） */
    private Double utilityCostDifference;
    
    /** AIアドバイス */
    private String aiAdvice;
}

