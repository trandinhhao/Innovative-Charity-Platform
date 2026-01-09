package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillResponse;
import dev.lhs.charity_backend.entity.Bid;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Skill;
import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.SkillAuctionMapper;
import dev.lhs.charity_backend.mapper.SkillMapper;
import dev.lhs.charity_backend.repository.BidRepository;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.SkillAuctionRepository;
import dev.lhs.charity_backend.repository.SkillRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.SkillAuctionService;
import dev.lhs.charity_backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final SkillAuctionMapper skillAuctionMapper;
    private final SkillAuctionRepository skillAuctionRepository;
    private final BidRepository bidRepository;
    private final SkillAuctionService skillAuctionService;

    @Override
//    @PostAuthorize("returnObject.username == authentication.name")
    public SkillResponse createSkill(Long userId, SkillCreationRequest request) {

        User user = userRepository.findUserById(userId);
        Campaign campaign = campaignRepository.findCampaignById(request.getCampaignId());

        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        Skill skill = skillMapper.toSkill(request);
        skill.setCurentBid(skill.getStartingBid());
        skill.setUser(user);
        skill.setCampaign(campaign);
        skill.setSkillAuctions(new ArrayList<>());

        return skillMapper.toSkillResponse(skillRepository.save(skill));
    }

    @Override
    public List<SkillResponse> getSkills() {
        return skillRepository.findAll()
                .stream().map(skillMapper::toSkillResponse).toList();
    }

    @Override
    public SkillResponse getSkill(Long skillId) {
        if (skillRepository.existsById(skillId)) {
            return skillMapper.toSkillResponse(skillRepository.findSkillById(skillId));
        } else throw new AppException(ErrorCode.SKILL_NOT_EXISTED);
    }

    @Override
    public String deleteSkill(Long skillId) {
        if (skillRepository.existsById(skillId)) {
            skillRepository.deleteById(skillId);
            return "Skill has been deleted";
        } else throw new AppException(ErrorCode.SKILL_NOT_EXISTED);
    }

    @Override
    @Transactional
    public SkillAuctionResponse auction(Long userId, Long skillId, BigDecimal bidAmount) {
        log.info("Processing auction bid: userId={}, skillId={}, bidAmount={}", userId, skillId, bidAmount);
        
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });
        
        // Validate skill
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> {
                    log.warn("Skill not found: skillId={}", skillId);
                    return new AppException(ErrorCode.SKILL_NOT_EXISTED);
                });

        // Tìm SkillAuction cho skill này (ưu tiên ACTIVE, nếu không có thì lấy mới nhất)
        List<SkillAuction> activeAuctions = skillAuctionRepository.findBySkillIdAndStatus(skillId, AuctionStatus.ACTIVE);
        log.info("Found {} active auctions for skillId={}", activeAuctions.size(), skillId);
        
        SkillAuction skillAuction;
        if (!activeAuctions.isEmpty()) {
            // Lấy SkillAuction ACTIVE đầu tiên (mới nhất)
            skillAuction = activeAuctions.get(0);
        } else {
            // Nếu không có ACTIVE, tìm SkillAuction mới nhất (có thể là PENDING)
            List<SkillAuction> allAuctions = skillAuctionRepository.findBySkillIdOrderByCreatedAtDesc(skillId);
            log.info("Found {} total auctions for skillId={}", allAuctions.size(), skillId);
            
            if (allAuctions.isEmpty()) {
                // Tự động tạo SkillAuction nếu chưa có bằng cách gọi SkillAuctionService
                log.info("No auction found for skillId={}, creating new auction automatically", skillId);
                SkillAuctionResponse auctionResponse = createSkillAuctionAutomatically(skill);
                // Lấy SkillAuction vừa tạo
                skillAuction = skillAuctionRepository.findById(auctionResponse.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));
                log.info("Created new SkillAuction: auctionId={}", skillAuction.getId());
            }
            
            skillAuction = allAuctions.get(0);
            log.info("Using non-active auction: auctionId={}, status={}", skillAuction.getId(), skillAuction.getStatus());
            
            // Validate status - chỉ cho phép ACTIVE hoặc PENDING (nếu chưa đến startTime)
            if (skillAuction.getStatus() != AuctionStatus.ACTIVE && skillAuction.getStatus() != AuctionStatus.PENDING) {
                log.warn("Auction is not active or pending: auctionId={}, status={}", skillAuction.getId(), skillAuction.getStatus());
                throw new AppException(ErrorCode.AUCTION_NOT_ACTIVE);
            }
            
            // Nếu là PENDING, kiểm tra đã đến startTime chưa
            if (skillAuction.getStatus() == AuctionStatus.PENDING) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(skillAuction.getStartTime())) {
                    log.warn("Auction has not started yet: auctionId={}, startTime={}, now={}", 
                            skillAuction.getId(), skillAuction.getStartTime(), now);
                    throw new AppException(ErrorCode.AUCTION_NOT_ACTIVE);
                }
                // Nếu đã đến startTime nhưng vẫn PENDING, tự động activate
                skillAuction.setStatus(AuctionStatus.ACTIVE);
                skillAuction.setUpdatedAt(now);
                skillAuction = skillAuctionRepository.save(skillAuction);
                log.info("Auto-activated auction: auctionId={}", skillAuction.getId());
            }
        }
        log.info("Using auction: auctionId={}, currentBid={}, startingBid={}", 
                skillAuction.getId(), skillAuction.getCurrentBid(), skillAuction.getStartingBid());
        
        // Validate bid amount
        BigDecimal currentBid = skillAuction.getCurrentBid();
        BigDecimal startingBid = skillAuction.getStartingBid();
        
        // Nếu currentBid = 0, bidAmount phải >= startingBid
        // Nếu currentBid > 0, bidAmount phải > currentBid
        boolean isValidBid;
        if (currentBid.compareTo(BigDecimal.ZERO) == 0) {
            // Chưa có bid nào, phải >= startingBid
            isValidBid = bidAmount.compareTo(startingBid) >= 0;
        } else {
            // Đã có bid, phải > currentBid
            isValidBid = bidAmount.compareTo(currentBid) > 0;
        }
        
        if (!isValidBid) {
            BigDecimal minBid = currentBid.compareTo(BigDecimal.ZERO) > 0 ? currentBid : startingBid;
            log.warn("Bid amount too low: bidAmount={}, minBid={}, currentBid={}, startingBid={}", 
                    bidAmount, minBid, currentBid, startingBid);
            throw new AppException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
        
        // Validate không tự outbid chính mình
        if (skillAuction.getHighestBidderId() != null && skillAuction.getHighestBidderId().equals(userId)) {
            log.warn("User trying to outbid themselves: userId={}, auctionId={}", userId, skillAuction.getId());
            throw new AppException(ErrorCode.BID_SELF_OUTBID);
        }
        
        // Validate chưa hết hạn
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(skillAuction.getEndTime())) {
            log.warn("Auction expired: auctionId={}, endTime={}, now={}", 
                    skillAuction.getId(), skillAuction.getEndTime(), now);
            throw new AppException(ErrorCode.AUCTION_EXPIRED);
        }
        
        try {
            // Cập nhật SkillAuction
            skillAuction.setCurrentBid(bidAmount);
            skillAuction.setHighestBidderId(userId);
            skillAuction.setUpdatedAt(now);
            skillAuction = skillAuctionRepository.save(skillAuction);
            log.info("Updated SkillAuction: auctionId={}, newCurrentBid={}", skillAuction.getId(), bidAmount);
            
            // Cập nhật Skill.curentBid
            skill.setCurentBid(bidAmount);
            skill = skillRepository.save(skill);
            log.info("Updated Skill: skillId={}, newCurentBid={}", skillId, bidAmount);
            
            // Tạo Bid record (audit trail)
            Bid bid = Bid.builder()
                    .skillAuction(skillAuction)
                    .bidder(user)
                    .bidAmount(bidAmount)
                    .clientTimestamp(System.currentTimeMillis())
                    .build();
            bid = bidRepository.save(bid);
            log.info("Created Bid record: bidId={}, auctionId={}, userId={}, amount={}", 
                    bid.getId(), skillAuction.getId(), userId, bidAmount);
            
            return skillAuctionMapper.toSkillAuctionResponse(skillAuction);
        } catch (Exception e) {
            log.error("Error processing auction bid: userId={}, skillId={}, bidAmount={}", 
                    userId, skillId, bidAmount, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    /**
     * Tự động tạo SkillAuction cho Skill nếu chưa có bằng cách gọi SkillAuctionService
     */
    private SkillAuctionResponse createSkillAuctionAutomatically(Skill skill) {
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy thông tin từ Skill
        BigDecimal startingBid = skill.getStartingBid() != null ? skill.getStartingBid() : BigDecimal.valueOf(100); // Default 100k VND
        BigDecimal targetAmount = skill.getTargetBid();
        
        // Xác định startTime: nếu startDate trong quá khứ hoặc null, dùng now
        LocalDateTime startTime;
        if (skill.getStartDate() != null && skill.getStartDate().isAfter(now)) {
            startTime = skill.getStartDate();
        } else {
            startTime = now; // Dùng now nếu startDate trong quá khứ hoặc null
        }
        
        // Xác định endTime: nếu endDate trong quá khứ hoặc null, dùng 7 ngày sau startTime
        LocalDateTime endTime;
        if (skill.getEndDate() != null && skill.getEndDate().isAfter(startTime)) {
            endTime = skill.getEndDate();
        } else {
            endTime = startTime.plusDays(7); // Dùng 7 ngày sau startTime nếu endDate không hợp lệ
        }
        
        // Skill owner là user tạo skill (skill.getUser())
        Long skillOwnerId = skill.getUser().getId();
        Long campaignId = skill.getCampaign().getId();
        
        // Tạo request để gọi SkillAuctionService.createAuction()
        SkillAuctionCreationRequest request = SkillAuctionCreationRequest.builder()
                .skillId(skill.getId())
                .skillOwnerId(skillOwnerId)
                .campaignId(campaignId)
                .startingBid(startingBid)
                .targetAmount(targetAmount)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        
        // Gọi SkillAuctionService để tạo SkillAuction
        SkillAuctionResponse auctionResponse = skillAuctionService.createAuction(request);
        log.info("Auto-created SkillAuction via SkillAuctionService: auctionId={}, skillId={}, startingBid={}, endTime={}", 
                auctionResponse.getId(), skill.getId(), startingBid, endTime);
        
        return auctionResponse;
    }
    
    @Override
    @Transactional
    public SkillAuctionResponse createAuctionAndBid(Long userId, Long skillId, BigDecimal bidAmount) {
         log.info("Creating auction and bid: userId={}, skillId={}, bidAmount={} (VND)", userId, skillId, bidAmount);
        
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });
        
        // Lấy Skill theo skillId
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> {
                    log.warn("Skill not found: skillId={}", skillId);
                    return new AppException(ErrorCode.SKILL_NOT_EXISTED);
                });
        
        // Frontend gửi VND thực tế, backend cần chuyển sang đơn vị nghìn để so sánh với DB
        // DB lưu theo đơn vị nghìn (ví dụ: 2000 VND -> 2 trong DB)
        BigDecimal bidAmountInThousand = bidAmount.divide(BigDecimal.valueOf(1000), 3, java.math.RoundingMode.HALF_UP);
        
        // So sánh với 3 thông số từ Skill
        BigDecimal skillCurrentBid = skill.getCurentBid(); // Đã là đơn vị nghìn
        BigDecimal skillStartingBid = skill.getStartingBid(); // Đã là đơn vị nghìn
        BigDecimal skillTargetBid = skill.getTargetBid(); // Đã là đơn vị nghìn
        
        log.info("Validating bid with Skill: bidAmountVND={}, bidAmountInThousand={}, skillCurrentBid={}, skillStartingBid={}, skillTargetBid={}", 
                bidAmount, bidAmountInThousand, skillCurrentBid, skillStartingBid, skillTargetBid);
        
        // Validate: bidAmount phải >= startingBid
        if (bidAmountInThousand.compareTo(skillStartingBid) < 0) {
            log.warn("Bid amount too low: bidAmountInThousand={}, skillStartingBid={}", bidAmountInThousand, skillStartingBid);
            throw new AppException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
        
        // Validate: bidAmount phải <= targetBid (nếu có)
        if (skillTargetBid != null && bidAmountInThousand.compareTo(skillTargetBid) > 0) {
            log.warn("Bid amount too high: bidAmountInThousand={}, skillTargetBid={}", bidAmountInThousand, skillTargetBid);
            throw new AppException(ErrorCode.BID_AMOUNT_TOO_LOW); // TODO: Add specific error for too high
        }
        
        // Nếu currentBid > 0, bidAmount phải > currentBid
        if (skillCurrentBid.compareTo(BigDecimal.ZERO) > 0 && bidAmountInThousand.compareTo(skillCurrentBid) <= 0) {
            log.warn("Bid amount must be greater than current bid: bidAmountInThousand={}, skillCurrentBid={}", 
                    bidAmountInThousand, skillCurrentBid);
            throw new AppException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
        
        // Validate chưa hết hạn - kiểm tra với Skill.endDate
        LocalDateTime now = LocalDateTime.now();
        if (skill.getEndDate() != null && now.isAfter(skill.getEndDate())) {
            log.warn("Skill expired: skillId={}, endDate={}, now={}", skillId, skill.getEndDate(), now);
            throw new AppException(ErrorCode.AUCTION_EXPIRED);
        }
        
        // Tạo SkillAuction mới trực tiếp từ Skill
        LocalDateTime startTime = skill.getStartDate() != null && skill.getStartDate().isAfter(now) 
                ? skill.getStartDate() 
                : now;
        LocalDateTime endTime = skill.getEndDate() != null && skill.getEndDate().isAfter(startTime)
                ? skill.getEndDate()
                : startTime.plusDays(7);
        
        User skillOwner = skill.getUser();
        Campaign campaign = skill.getCampaign();
        
        SkillAuction skillAuction = SkillAuction.builder()
                .skill(skill)
                .skillOwner(skillOwner)
                .campaign(campaign)
                .startingBid(skillStartingBid)
                .currentBid(bidAmountInThousand) // Set ngay bidAmount
                .targetAmount(skillTargetBid)
                .startTime(startTime)
                .endTime(endTime)
                .status(AuctionStatus.ACTIVE) // Luôn ACTIVE vì đang đặt giá ngay
                .highestBidderId(userId)
                .bids(new ArrayList<>())
                .transactions(new ArrayList<>())
                .build();
        
        skillAuction = skillAuctionRepository.save(skillAuction);
        log.info("Created new SkillAuction: auctionId={}, skillId={}, currentBid={} (thousand)", 
                skillAuction.getId(), skillId, bidAmountInThousand);
        
        // Cập nhật Skill.curentBid (lưu theo đơn vị nghìn)
        skill.setCurentBid(bidAmountInThousand);
        skill = skillRepository.save(skill);
        log.info("Updated Skill: skillId={}, newCurentBid={} (thousand)", skillId, bidAmountInThousand);
        
        // Tạo Bid record (audit trail) - lưu theo đơn vị nghìn
        Bid bid = Bid.builder()
                .skillAuction(skillAuction)
                .bidder(user)
                .bidAmount(bidAmountInThousand)
                .clientTimestamp(System.currentTimeMillis())
                .build();
        bid = bidRepository.save(bid);
        log.info("Created Bid record: bidId={}, auctionId={}, userId={}, amount={} (thousand)", 
                bid.getId(), skillAuction.getId(), userId, bidAmountInThousand);
        
        return skillAuctionMapper.toSkillAuctionResponse(skillAuction);
    }

}
