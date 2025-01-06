package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.PostCategory;
import com.withgpt.gpt.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByAuthorEmailAndSubscriberEmail(String authorEmail, String subscriberEmail);

    Optional<Subscription> findByAuthorEmailAndSubscriberEmail(String authorEmail, String subscriberEmail);

    Optional<Subscription> findBySubscriberEmailAndAuthorEmail(String subscriberEmail, String authorEmail);

    void deleteByAuthorEmailAndSubscriberEmail(String authorEmail, String subscriberEmail);

    boolean existsBySubscriberEmailAndAuthorEmailAndCategory(String subscriberEmail, String authorEmail, PostCategory category);

    Optional<Subscription> findBySubscriberEmailAndAuthorEmailAndCategory(String subscriberEmail, String authorEmail, PostCategory category);

    List<Subscription> findBySubscriberEmail(String subscriberEmail);

    void deleteBySubscriberEmailAndAuthorEmailAndCategory(String subscriberEmail, String authorEmail, PostCategory category);
}
