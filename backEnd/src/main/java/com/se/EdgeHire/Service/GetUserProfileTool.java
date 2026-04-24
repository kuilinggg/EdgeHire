package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Repository.InfoRepository;
import com.se.EdgeHire.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetUserProfileTool implements OfferAgentTool {
    private final UserRepository userRepository;
    private final InfoRepository infoRepository;

    @Override
    public String name() {
        return "get_user_profile";
    }

    @Override
    public String description() {
        return "Read deterministic user profile from t_user and t_info.";
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        Map<String, Object> output = new LinkedHashMap<>();
        userRepository.findById(context.getUserId()).ifPresent(user -> {
            output.put("userId", user.getId());
            output.put("username", user.getUsername());
            output.put("role", user.getRole());
        });

        List<Info> infos = infoRepository.findByUserId(context.getUserId());
        if (!infos.isEmpty()) {
            Info info = infos.get(0);
            output.put("realName", info.getRealname());
            output.put("email", info.getEmail());
            output.put("phone", info.getPhone());
        }
        output.put("summary", "loaded user profile and contact fields");
        return output;
    }
}
