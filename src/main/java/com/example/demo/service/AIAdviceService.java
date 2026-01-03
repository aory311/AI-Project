package com.example.demo.service;

import com.example.demo.dto.SimulationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * AIアドバイスサービス
 */
@Service
public class AIAdviceService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAdviceService.class);
    
    private final ChatClient chatClient;
    
    public AIAdviceService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * 入力データに基づいた営業トークを生成
     * 
     * @param input シミュレーション入力データ
     * @param monthlyPayment 毎月の返済額
     * @param utilityCostDifference 光熱費差額
     * @return AIアドバイス（営業トーク）
     */
    public String generateAdvice(SimulationInput input, long monthlyPayment, double utilityCostDifference) {
        try {
            String prompt = buildPrompt(input, monthlyPayment, utilityCostDifference);
            logger.info("AIアドバイス生成を開始します。プロンプト長: {}", prompt.length());
            
            String advice = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            logger.info("AIアドバイス生成成功。レスポンス長: {}", advice != null ? advice.length() : 0);
            
            return advice != null && !advice.isEmpty() ? advice : generateDefaultAdvice(input, monthlyPayment, utilityCostDifference);
        } catch (Exception e) {
            // エラーが発生した場合は、詳細なログを出力
            logger.error("AIアドバイス生成中にエラーが発生しました", e);
            logger.error("エラーメッセージ: {}", e.getMessage());
            logger.error("エラークラス: {}", e.getClass().getName());
            
            // エラーの種類に応じて適切なメッセージを返す
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("insufficient_quota")) {
                return generateDefaultAdvice(input, monthlyPayment, utilityCostDifference) + 
                       "\n\n※注: OpenAI APIの使用上限に達しているため、AI生成機能は一時的に利用できません。上記は自動生成された一般的なアドバイスです。";
            } else if (errorMessage != null && errorMessage.contains("429")) {
                return generateDefaultAdvice(input, monthlyPayment, utilityCostDifference) + 
                       "\n\n※注: APIの利用制限に達しているため、AI生成機能は一時的に利用できません。上記は自動生成された一般的なアドバイスです。";
            }
            
            // その他のエラーの場合はデフォルトアドバイスを返す
            return generateDefaultAdvice(input, monthlyPayment, utilityCostDifference);
        }
    }
    
    /**
     * デフォルトのアドバイスを生成（AIが使えない場合の代替）
     */
    private String generateDefaultAdvice(SimulationInput input, long monthlyPayment, double utilityCostDifference) {
        StringBuilder advice = new StringBuilder();
        
        advice.append("ご入力いただいた情報を基に、以下の点をご提案いたします。\n\n");
        
        // 年収と返済額の比較
        double annualIncomeYen = input.getAnnualIncome() * 10000;
        double monthlyIncome = annualIncomeYen / 12;
        double paymentRatio = (monthlyPayment / monthlyIncome) * 100;
        
        advice.append("【返済額について】\n");
        advice.append("年収").append(input.getAnnualIncome()).append("万円の場合、月収は約").append(String.format("%.0f", monthlyIncome)).append("円となります。\n");
        advice.append("返済額").append(String.format("%,d", monthlyPayment)).append("円は月収の約").append(String.format("%.1f", paymentRatio)).append("%です。\n");
        
        if (paymentRatio <= 25) {
            advice.append("この返済額は適正な範囲内です。無理のない返済計画といえます。\n");
        } else if (paymentRatio <= 30) {
            advice.append("この返済額は一般的な基準内です。家計とのバランスを確認しながら検討することをお勧めします。\n");
        } else {
            advice.append("返済比率がやや高めです。家計の他の支出とのバランスを慎重に検討してください。\n");
        }
        
        // 現在の家賃との比較
        advice.append("\n【現在の家賃との比較】\n");
        if (monthlyPayment <= input.getCurrentRent()) {
            long difference = input.getCurrentRent() - monthlyPayment;
            advice.append("返済額は現在の家賃（").append(String.format("%,d", input.getCurrentRent())).append("円）よりも")
                  .append(String.format("%,d", difference)).append("円少なくなります。\n");
            advice.append("家計の負担が軽減されるため、検討の価値があります。\n");
        } else {
            long difference = monthlyPayment - input.getCurrentRent();
            advice.append("返済額は現在の家賃（").append(String.format("%,d", input.getCurrentRent())).append("円）よりも")
                  .append(String.format("%,d", difference)).append("円多くなりますが、\n");
            advice.append("資産形成という点では大きな違いがあります。\n");
        }
        
        // 光熱費について
        if (utilityCostDifference > 0) {
            advice.append("\n【光熱費の節約効果】\n");
            advice.append("大手HMと比較して、月額約").append(String.format("%,.0f", utilityCostDifference)).append("円の光熱費節約が期待できます。\n");
            advice.append("年間では約").append(String.format("%,.0f", utilityCostDifference * 12)).append("円の節約となります。\n");
        }
        
        // 家族構成に応じたアドバイス
        advice.append("\n【").append(input.getFamilyComposition()).append("のご家族におすすめ】\n");
        if (input.getFamilyComposition().contains("子")) {
            advice.append("お子様がいらっしゃるご家庭では、長期的な資産形成と教育資金計画の両立が重要です。\n");
            advice.append("住宅購入は大きな投資となりますが、将来の資産形成につながります。\n");
        } else {
            advice.append("ご家族での生活設計を考える上で、住宅購入は重要な選択肢です。\n");
            advice.append("ご予算と将来設計を踏まえて、慎重にご検討ください。\n");
        }
        
        return advice.toString();
    }
    
    /**
     * AIプロンプトを構築
     */
    private String buildPrompt(SimulationInput input, long monthlyPayment, double utilityCostDifference) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("あなたは住宅営業のプロフェッショナルです。以下の情報を基に、");
        prompt.append("顧客に寄り添った親しみやすい営業トークを生成してください。\n\n");
        
        // 計算済みの分析情報を含める
        double annualIncomeYen = input.getAnnualIncome() * 10000.0;
        double monthlyIncome = annualIncomeYen / 12.0;
        double paymentRatio = (monthlyPayment / monthlyIncome) * 100.0;
        long rentDifference = monthlyPayment - input.getCurrentRent();
        
        prompt.append("【顧客情報】\n");
        prompt.append("年収: ").append(input.getAnnualIncome()).append("万円\n");
        prompt.append("予算: ").append(input.getBudget()).append("万円\n");
        prompt.append("現在の家賃: ").append(String.format("%,d", input.getCurrentRent())).append("円/月\n");
        prompt.append("家族構成: ").append(input.getFamilyComposition()).append("\n\n");
        
        prompt.append("【試算結果と分析】\n");
        prompt.append("毎月の返済額: ").append(String.format("%,d", monthlyPayment)).append("円\n");
        prompt.append("月収: 約").append(String.format("%,.0f", monthlyIncome)).append("円（年収").append(input.getAnnualIncome()).append("万円÷12ヶ月）\n");
        prompt.append("返済比率: 約").append(String.format("%.1f", paymentRatio)).append("%（返済額÷月収）\n");
        if (paymentRatio <= 25) {
            prompt.append("→ 返済比率は適正な範囲内です。無理のない返済計画といえます。\n");
        } else if (paymentRatio <= 30) {
            prompt.append("→ 返済比率は一般的な基準内です。家計とのバランスを確認しながら検討することをお勧めします。\n");
        } else {
            prompt.append("→ 返済比率がやや高めです。家計の他の支出とのバランスを慎重に検討してください。\n");
        }
        
        prompt.append("\n【現在の家賃との比較】\n");
        if (rentDifference < 0) {
            prompt.append("返済額は現在の家賃よりも").append(String.format("%,d", Math.abs(rentDifference))).append("円少なくなります。\n");
            prompt.append("→ 家計の負担が軽減されるため、検討の価値があります。\n");
        } else if (rentDifference > 0) {
            prompt.append("返済額は現在の家賃よりも").append(String.format("%,d", rentDifference)).append("円多くなりますが、\n");
            prompt.append("→ 資産形成という点では大きな違いがあります。\n");
        } else {
            prompt.append("返済額と現在の家賃は同じです。\n");
            prompt.append("→ 家賃がそのまま資産形成に回る形になります。\n");
        }
        
        prompt.append("\n【光熱費について】\n");
        if (utilityCostDifference > 0) {
            prompt.append("大手HMとの光熱費差額: 月額約").append(String.format("%,.0f", utilityCostDifference)).append("円の節約\n");
            prompt.append("年間の節約額: 約").append(String.format("%,.0f", utilityCostDifference * 12)).append("円\n");
            prompt.append("→ 長期的に見ると大きなメリットとなります。\n");
        } else if (utilityCostDifference < 0) {
            prompt.append("大手HMとの光熱費差額: 月額約").append(String.format("%,.0f", Math.abs(utilityCostDifference))).append("円高くなります\n");
        } else {
            prompt.append("光熱費: 大手HMと同等\n");
        }
        
        prompt.append("\n【営業トークの要件】\n");
        prompt.append("以下の内容を必ず含めて、親しみやすく丁寧な口調で営業トークを生成してください：\n");
        prompt.append("1. 返済額について（返済比率の分析を含む）\n");
        prompt.append("2. 現在の家賃との比較（差額を具体的に言及）\n");
        prompt.append("3. 光熱費の節約効果（差額がある場合）\n");
        prompt.append("4. 家族構成に応じた提案\n");
        prompt.append("5. 数字を効果的に活用して説得力のある内容\n");
        prompt.append("6. 押し売りではなく、顧客の立場に立った提案\n");
        prompt.append("\n営業トークを生成してください（400文字程度）:");
        
        return prompt.toString();
    }
}

