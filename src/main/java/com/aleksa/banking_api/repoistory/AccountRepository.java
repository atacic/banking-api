package com.aleksa.banking_api.repoistory;

import com.aleksa.banking_api.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a from Account a
        where a.accountNumber in (:from, :to)
        order by a.accountNumber
    """)
    List<Account> findBothByAccountNumberWithLock(String from, String to);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdWithLock(Long id);
}
