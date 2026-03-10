package lk.icbt.eduvision.eduvision360.communication.repository;

import lk.icbt.eduvision.eduvision360.communication.model.DirectMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRepository extends MongoRepository<DirectMessage, String> {

    List<DirectMessage> findByReceiverIdOrderByCreatedAtDesc(String receiverId);

    List<DirectMessage> findBySenderIdOrderByCreatedAtDesc(String senderId);

    Optional<DirectMessage> findByIdAndReceiverId(String id, String receiverId);

    List<DirectMessage> findTop5ByOrderByCreatedAtDesc();
}