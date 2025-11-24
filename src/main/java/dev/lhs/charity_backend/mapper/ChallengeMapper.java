package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.ChallengeCreationRequest;
import dev.lhs.charity_backend.dto.response.ChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserChallengeResponse;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.entity.UserChallenge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChallengeMapper {
    @Mapping(target = "campaign", ignore = true)
    Challenge toChallenge(ChallengeCreationRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "campaignId", source = "campaign.id")
    ChallengeResponse toChallengeResponse(Challenge challenge);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "challengeId", source = "challenge.id")
    UserChallengeResponse toUserChallengeResponse(UserChallenge challenge);
}
