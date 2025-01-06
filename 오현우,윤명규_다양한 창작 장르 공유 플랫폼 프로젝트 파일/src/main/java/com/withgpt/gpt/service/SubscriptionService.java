package com.withgpt.gpt.service;

import com.withgpt.gpt.controller.SubscriptionController;
import com.withgpt.gpt.model.PostCategory; // PostCategory import 추가
import com.withgpt.gpt.model.Subscription;
import com.withgpt.gpt.repository.PostRepository;
import com.withgpt.gpt.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {


    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    private final SubscriptionRepository subscriptionRepository;
    private final PostRepository postRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, PostRepository postRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.postRepository = postRepository;
    }

    // 구독 추가 메서드
    public void subscribe(String authorEmail, String subscriberEmail) {
        // 이미 구독 중인지 확인
        if (subscriptionRepository.existsByAuthorEmailAndSubscriberEmail(authorEmail, subscriberEmail)) {
            throw new IllegalStateException("이미 구독하고 있는 사용자입니다.");
        }

        // 새로운 구독 생성 및 저장
        Subscription subscription = new Subscription(subscriberEmail, authorEmail);
        subscriptionRepository.save(subscription);
    }

    // 구독 취소 메서드
    public void unsubscribe(String authorEmail, String subscriberEmail) {
        // 구독 찾기
        Subscription subscription = subscriptionRepository.findByAuthorEmailAndSubscriberEmail(authorEmail, subscriberEmail)
                .orElseThrow(() -> new IllegalStateException("구독하지 않은 사용자입니다."));

        // 구독 삭제
        subscriptionRepository.delete(subscription);
    }

    // 구독 여부 확인 메서드
    public boolean isSubscribed(String subscriberEmail, String authorEmail) {
        return subscriptionRepository
                .findBySubscriberEmailAndAuthorEmail(subscriberEmail, authorEmail)
                .isPresent(); // 구독 여부를 확인
    }

    // 구독 목록 조회 메서드 (구독자의 이메일을 기준으로)
    public List<Subscription> getSubscriptionsBySubscriberEmail(String subscriberEmail) {
        return subscriptionRepository.findBySubscriberEmail(subscriberEmail);
    }

    // 구독 상태를 토글하는 메서드 (구독/취소 통합 처리)
    @Transactional
    public boolean toggleSubscription(String authorEmail, String subscriberEmail) {
        // 구독 여부 확인 및 상태 변경
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findBySubscriberEmailAndAuthorEmail(subscriberEmail, authorEmail);

        if (existingSubscription.isPresent()) {
            subscriptionRepository.delete(existingSubscription.get());
            logger.info("구독 취소됨: 작성자={}, 구독자={}", authorEmail, subscriberEmail);
            return false; // 구독 취소됨
        } else {
            Subscription newSubscription = new Subscription(subscriberEmail, authorEmail);
            subscriptionRepository.save(newSubscription);
            logger.info("구독 성공: 작성자={}, 구독자={}", authorEmail, subscriberEmail);
            return true; // 구독 성공
        }
    }




}
