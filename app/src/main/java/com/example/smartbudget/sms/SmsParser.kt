package com.example.smartbudget.sms

data class ParsedExpense(val amount: Long, val merchant: String, val category: String)

object SmsParser {

    private val amountRegex = Regex("""([\d,]+)원""")

    private val cardPatterns = listOf(
        // 신한카드: [신한카드] 승인 15,000원 스타벅스
        Regex("""신한카드.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""신한카드.*?([\d,]+)원\s+(.+)"""),
        // 국민카드: [국민카드승인] 15,000원 스타벅스
        Regex("""국민카드.*?([\d,]+)원\s+(.+?)\s*\d"""),
        Regex("""국민카드.*?([\d,]+)원\s+(.+)"""),
        // 삼성카드: 삼성카드(1234) 15,000원 스타벅스
        Regex("""삼성카드.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""삼성카드.*?([\d,]+)원\s+(.+)"""),
        // 현대카드: [현대카드] 15,000원 스타벅스
        Regex("""현대카드.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""현대카드.*?([\d,]+)원\s+(.+)"""),
        // 하나카드
        Regex("""하나카드.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""하나카드.*?([\d,]+)원\s+(.+)"""),
        // 롯데카드
        Regex("""롯데카드.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""롯데카드.*?([\d,]+)원\s+(.+)"""),
        // NH농협카드
        Regex("""농협.*?([\d,]+)원\s+(.+?)\s*[\(\[]"""),
        Regex("""농협.*?([\d,]+)원\s+(.+)"""),
    )

    private val categoryKeywords = mapOf(
        "카페" to listOf("스타벅스", "카페", "커피", "이디야", "투썸", "빽다방", "할리스"),
        "식료품" to listOf("이마트", "홈플러스", "롯데마트", "쿠팡", "마트", "편의점", "GS25", "CU", "세븐일레븐", "미니스톱"),
        "식당" to listOf("맥도날드", "버거킹", "롯데리아", "KFC", "치킨", "피자", "배달", "식당", "음식"),
        "교통" to listOf("주유", "기름", "SK에너지", "GS칼텍스", "현대오일", "버스", "지하철", "택시", "카카오T"),
        "의료" to listOf("병원", "약국", "의원", "클리닉", "한의원", "치과"),
        "쇼핑" to listOf("무신사", "올리브영", "다이소", "쇼핑", "백화점", "아울렛"),
    )

    fun parse(smsBody: String): ParsedExpense? {
        val isCardSms = listOf("카드", "승인", "결제").any { smsBody.contains(it) }
        if (!isCardSms) return null

        for (pattern in cardPatterns) {
            val match = pattern.find(smsBody) ?: continue
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: continue
            val merchant = match.groupValues[2].trim().take(20)
            if (amount <= 0 || merchant.isBlank()) continue
            return ParsedExpense(amount, merchant, classifyCategory(merchant))
        }

        // 패턴 매칭 실패 시 금액만 추출
        val amount = amountRegex.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toLongOrNull()
            ?: return null
        return ParsedExpense(amount, "알수없음", "기타")
    }

    private fun classifyCategory(merchant: String): String {
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { merchant.contains(it) }) return category
        }
        return "기타"
    }
}
