package com.geovannycode.wallet.movement.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MovementRepository : JpaRepository<Movement, UUID>