package com.example.demo.service;

import org.springframework.stereotype.Service;

/**
 * 計算ロジックサービス
 */
@Service
public class CalculationService {
    
    /** ローン金利（年率） */
    private static final double LOAN_INTEREST_RATE = 0.01; // 1.0%
    
    /** ローン返済年数 */
    private static final int LOAN_TERM_YEARS = 35;
    
    /** 大手HMの平均光熱費（円/月） */
    private static final long STANDARD_UTILITY_COST = 25000;
    
    /**
     * 毎月の返済額を計算
     * 
     * @param budget 予算（万円）
     * @return 毎月の返済額（円）
     */
    public long calculateMonthlyPayment(int budget) {
        // 予算を円に変換（借入金額として90%を想定）
        long loanAmount = (long) (budget * 10000 * 0.9);
        
        // 月利を計算
        double monthlyRate = LOAN_INTEREST_RATE / 12.0;
        
        // 返済回数（月数）
        int numPayments = LOAN_TERM_YEARS * 12;
        
        // 元利均等返済の計算式
        if (monthlyRate == 0) {
            return loanAmount / numPayments;
        }
        
        double payment = loanAmount * (monthlyRate * Math.pow(1 + monthlyRate, numPayments)) 
                        / (Math.pow(1 + monthlyRate, numPayments) - 1);
        
        return Math.round(payment);
    }
    
    /**
     * 家族構成に基づく想定光熱費を計算
     * 
     * @param familyComposition 家族構成
     * @return 想定光熱費（円/月）
     */
    public long calculateEstimatedUtilityCost(String familyComposition) {
        // 家族構成に応じた光熱費の目安を計算
        int familySize = estimateFamilySize(familyComposition);
        
        // 1人あたりの平均光熱費（円/月）
        long baseCost = 15000;
        
        // 家族数に応じて調整（2人目以降は少し割安）
        long totalCost = baseCost + (familySize - 1) * 8000;
        
        return totalCost;
    }
    
    /**
     * 家族構成から家族人数を推定
     * 
     * @param familyComposition 家族構成
     * @return 家族人数
     */
    private int estimateFamilySize(String familyComposition) {
        if (familyComposition == null || familyComposition.isEmpty()) {
            return 2;
        }
    
        // 「その他:5」のような形式で送られてきた場合
        if (familyComposition.startsWith("その他") && familyComposition.contains(":")) {
            try {
                String numPart = familyComposition.substring(familyComposition.indexOf(":") + 1);
                return Integer.parseInt(numPart.trim());
            } catch (NumberFormatException e) {
                return 2; // 変換エラー時は標準の2人を返す
            }
        }
    
        // 既存のパターン判定（そのまま）
        String lower = familyComposition.toLowerCase();
        if (lower.contains("単身")) return 1;
        if (lower.contains("夫婦2人")) return 2;
        if (lower.contains("夫婦+子1人")) return 3;
        if (lower.contains("夫婦+子2人")) return 4;
        if (lower.contains("夫婦+子3人")) return 5;
        
        return 2;
    }
    
    /**
     * 大手HMとの光熱費の差額を計算
     * 
     * @param familyComposition 家族構成
     * @return 差額（円/月、正の値は当社の方が安い、負の値は高い）
     */
    public double calculateUtilityCostDifference(String familyComposition) {
        double estimatedCost = calculateEstimatedUtilityCostDouble(familyComposition);
        
        // 大手HMとの差額を計算（当社の方が安い場合は正の値）
        return STANDARD_UTILITY_COST - estimatedCost;
    }
    
    /**
     * 家族構成に基づく想定光熱費を計算（double型で返す）
     * 
     * @param familyComposition 家族構成
     * @return 想定光熱費（円/月）
     */
    private double calculateEstimatedUtilityCostDouble(String familyComposition) {
        int familySize = estimateFamilySize(familyComposition);
        
        // 1人あたりの平均光熱費（円/月）
        double baseCost = 15000.0;
        
        // 家族数に応じて調整（2人目以降は少し割安）
        // より詳細な計算のために小数点を含める
        double totalCost = baseCost + (familySize - 1) * (baseCost/2);
        
        return totalCost;
    }
}

