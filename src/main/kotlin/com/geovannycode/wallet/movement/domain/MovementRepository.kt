package com.geovannycode.wallet.movement.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MovementRepository : JpaRepository<MovementEntity, UUID>