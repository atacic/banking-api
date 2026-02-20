package com.aleksa.banking_api.repoistory;

import com.aleksa.banking_api.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> { }
