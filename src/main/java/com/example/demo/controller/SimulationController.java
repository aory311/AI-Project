package com.example.demo.controller;

import com.example.demo.dto.SimulationInput;
import com.example.demo.dto.SimulationResult;
import com.example.demo.service.AIAdviceService;
import com.example.demo.service.CalculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * シミュレーションコントローラー
 */
@Controller
@RequestMapping("/simulation")
public class SimulationController {
    
    private final CalculationService calculationService;
    private final AIAdviceService aiAdviceService;
    
    public SimulationController(CalculationService calculationService, 
                                AIAdviceService aiAdviceService) {
        this.calculationService = calculationService;
        this.aiAdviceService = aiAdviceService;
    }
    
    /**
     * 入力画面を表示
     */
    @GetMapping("/input")
    public String showInputForm(Model model) {
        model.addAttribute("simulationInput", new SimulationInput());
        return "input";
    }
    
    /**
     * シミュレーション結果を計算して表示
     */
    @PostMapping("/calculate")
    public String calculate(SimulationInput input, Model model) {
        try {
            // 入力値の検証
            if (input.getAnnualIncome() == null || input.getBudget() == null || 
                input.getCurrentRent() == null || input.getFamilyComposition() == null ||
                input.getFamilyComposition().isEmpty()) {
                model.addAttribute("error", "すべての項目を入力してください。");
                model.addAttribute("simulationInput", input);
                return "input";
            }

            // 家族構成を"その他:6" のような形式にする
            String compositionToProcess = input.getFamilyComposition();
            if ("その他".equals(compositionToProcess) && input.getCustomFamilySize() != null) {
            compositionToProcess = "その他:" + input.getCustomFamilySize();
            }
            
            // 計算処理
            long monthlyPayment = calculationService.calculateMonthlyPayment(input.getBudget());
            double utilityCostDifference = calculationService.calculateUtilityCostDifference(compositionToProcess);
            long rentDifference = monthlyPayment - input.getCurrentRent();
            
            // AIアドバイス生成
            String aiAdvice = aiAdviceService.generateAdvice(input, monthlyPayment, utilityCostDifference);
            
            // 結果を設定
            SimulationResult result = new SimulationResult();
            result.setInput(input);
            result.setMonthlyPayment(monthlyPayment);
            result.setRentDifference(rentDifference);
            result.setUtilityCostDifference(utilityCostDifference);
            result.setAiAdvice(aiAdvice);
            
            model.addAttribute("result", result);
            
            return "result";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "エラーが発生しました: " + e.getMessage());
            model.addAttribute("simulationInput", input);
            return "input";
        }
    }
}

