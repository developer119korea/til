package com.developer119.kafka

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaController(
    private val kafkaProducer: KafkaProducer
) {
    @PostMapping("/send")
    fun sendMessage(@RequestParam message: String): String {
        val topic = "test-topic"  // 토픽명은 설정에 맞게 변경해주세요
        kafkaProducer.sendMessage(topic, message)
        return "메시지 전송 완료: $message"
    }
}