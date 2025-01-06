package com.withgpt.gpt.controller;

import com.withgpt.gpt.dto.SponsorshipDTO;
import com.withgpt.gpt.model.Sponsorship;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.service.SponsorshipService;
import com.withgpt.gpt.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sponsorship")
@RequiredArgsConstructor
public class SponsorshipController {

    private final SponsorshipService sponsorshipService;
    private final UserService userService;

    @GetMapping("/new")
    public String showSponsorshipForm(@RequestParam("authorEmail") String authorEmail, Model model) {
        model.addAttribute("authorEmail", authorEmail);
        return "sponsorshipForm"; // 후원 폼 페이지
    }

    @PostMapping("/checkout")
    public String processSponsorship(@RequestParam("authorEmail") String authorEmail,
                                     @RequestParam("amount") int amount,
                                     Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }

        String userEmail = principal.getName(); // 후원자의 이메일
        sponsorshipService.createSponsorship(userEmail, authorEmail, amount);

        // 후원받은 사용자의 ID를 찾아서 상세 페이지로 리다이렉트
        User author = userService.findByEmail(authorEmail);
        if (author == null) {
            return "redirect:/error"; // 사용자가 없는 경우 에러 페이지로 리다이렉트
        }

        Long authorId = author.getId();
        return "redirect:/user/" + authorId; // 후원받은 사용자의 상세 페이지로 리다이렉트
    }


        @GetMapping("/mySponsorships")
    public String showMySponsorships(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }

        String userEmail = principal.getName(); // 현재 로그인한 사용자의 이메일을 가져옴
        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<SponsorshipDTO> sponsorshipDTOs = user.getSponsorships().stream().map(sponsorship ->
                new SponsorshipDTO(
                        sponsorship.getSponsor().getNickname(),
                        sponsorship.getAmount(),
                        sponsorship.getSponsorshipDate()
                )
        ).collect(Collectors.toList());

        model.addAttribute("sponsorships", sponsorshipDTOs);
        model.addAttribute("totalSponsorshipAmount", user.getTotalSponsorshipAmount());
        return "mySponsorships"; // 마이페이지에서 후원 내역을 표시할 HTML 파일
    }
}
