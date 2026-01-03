package com.example.demo.dto;

import lombok.Data;

/**
 * シミュレーション入力データ
 */
@Data
public class SimulationInput {
    /** 年収（万円） */
    private Integer annualIncome;
    
    /** 予算（万円） */
    private Integer budget;
    
    /** 現在の家賃（円） */
    private Integer currentRent;
    
    /** 家族構成（例: 夫婦2人、夫婦+子2人など） */
    private String familyComposition;

    /** 家族の人数（その他を選択した場合） */
    private Integer customFamilySize;
}

