package com.perno97.financialmanagement.database

import android.content.Context
import android.util.Log
import com.perno97.financialmanagement.notifications.NotifyManager
import com.perno97.financialmanagement.viewmodels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object PeriodicMovementsChecker {
    private const val logTag = "PeriodicMovementsChecker"

    fun check(
        context: Context,
        appViewModel: AppViewModel,
        scope: CoroutineScope,
        lastAccess: LocalDate?,
        callback: (() -> Unit)?,
        periodicMovement: PeriodicMovement?,
    ) {
        Log.i(logTag, "Called check")
        scope.launch {
            val list =
                if (periodicMovement == null) {
                    appViewModel.getAllPeriodicMovements()
                } else {
                    listOf(periodicMovement)
                }
            for (periodicMov in list) {
                var movement: Movement? = null
                var incomingMovement: IncomingMovement? = null
                if (lastAccess != null) {
                    // Search for last added movement of this periodic
                    movement = appViewModel.getLatestPeriodicMovement(
                        periodicMovementId = periodicMov.periodicMovementId,
                        dateTo = LocalDate.now(),
                        dateFrom = lastAccess
                    )
                    // Search for last added incoming movement of this periodic
                    incomingMovement = appViewModel.getLatestIncomingPeriodic(
                        periodicMovementId = periodicMov.periodicMovementId,
                        dateTo = LocalDate.now().plusWeeks(2),
                        dateFrom = lastAccess
                    )
                }

                val calculatedMovements =
                    if (movement != null) {
                        // Generate movements starting the day after the found one
                        getMovementsFromPeriodicMovement(
                            periodicMov,
                            movement.date.plusDays(1),
                            LocalDate.now()
                                .plusWeeks(2) // Generating incoming movements up to 2 weeks
                        )
                    } else {
                        // No movements found before today and after last access or periodic movement's date
                        getMovementsFromPeriodicMovement(
                            periodicMov,
                            lastAccess ?: periodicMov.date,
                            LocalDate.now().plusWeeks(2)
                        )
                    }
                var amountsSum = 0f
                for (date in calculatedMovements.keys) {
                    val movDate = LocalDate.parse(date)
                    val mov: PeriodicMovement = calculatedMovements[date]!!
                    val amount = mov.amount
                    if (movDate.isAfter(LocalDate.now())) {
                        // Movement is an incoming movement
                        if (incomingMovement != null && movDate.isAfter(incomingMovement.date)) {
                            // Add it only if it's new
                            val movementId = appViewModel.insert(
                                IncomingMovement(
                                    date = movDate,
                                    amount = amount,
                                    category = mov.category,
                                    title = mov.title,
                                    notes = mov.notes,
                                    notify = mov.notify,
                                    periodicMovementId = mov.periodicMovementId
                                )
                            )
                            if (mov.notify) {
                                NotifyManager.setAlarm(
                                    context,
                                    movementId.toInt(),
                                    mov.title,
                                    mov.category,
                                    mov.amount,
                                    movDate
                                )
                            }
                        } else if (incomingMovement == null) {
                            // Add it only if it's new
                            appViewModel.insert(
                                IncomingMovement(
                                    date = movDate,
                                    amount = amount,
                                    category = mov.category,
                                    title = mov.title,
                                    notes = mov.notes,
                                    notify = mov.notify,
                                    periodicMovementId = mov.periodicMovementId
                                )
                            )
                        }
                    } else {
                        appViewModel.insert(
                            Movement(
                                date = movDate,
                                amount = amount,
                                category = mov.category,
                                title = mov.title,
                                notes = mov.notes,
                                periodicMovementId = mov.periodicMovementId
                            )
                        )
                        amountsSum += amount
                    }
                }
                if (amountsSum != 0f)
                    appViewModel.updateAssets(appViewModel.getCurrentAssetDefault() + amountsSum)
            }
            if (callback != null) {
                callback()
            }
        }
    }

    private fun getMovementsFromPeriodicMovement(
        periodicMovement: PeriodicMovement,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): HashMap<String, PeriodicMovement> {
        val movements = HashMap<String, PeriodicMovement>()
        val days = periodicMovement.days.toLong()
        val months = periodicMovement.months.toLong()
        // ----------------- DAYS -----------------
        if (days != 0L) {
            var currentDate = periodicMovement.date
            var checkAfter = true
            var checkBefore = true
            if (currentDate.isBefore(dateFrom)) {
                while (currentDate.isBefore(dateFrom)) {
                    currentDate = currentDate.plusDays(days)
                }
                checkBefore = false
            }
            if (currentDate.isAfter(dateTo)) {
                while (currentDate.isAfter(dateTo)) {
                    currentDate = currentDate.minusDays(days)
                }
                checkAfter = false
            }

            var currentBefore = currentDate
            var currentAfter = currentDate
            while (checkAfter && !currentAfter.isAfter(dateTo)) {
                movements[currentAfter.toString()] = periodicMovement
                currentAfter = currentAfter.plusDays(days)
            }
            while (checkBefore && !currentBefore.isBefore(dateFrom)) {
                movements[currentBefore.toString()] = periodicMovement
                currentBefore = currentBefore.minusDays(days)
            }
        }
        // ----------------- MONTHS -----------------
        if (months != 0L) {
            var currentDate = periodicMovement.date
            var checkAfter = true
            var checkBefore = true
            if (currentDate.isBefore(dateFrom)) {
                while (currentDate.isBefore(dateFrom)) {
                    currentDate = currentDate.plusMonths(months)
                }
                checkBefore = false
            }
            if (currentDate.isAfter(dateTo)) {
                while (currentDate.isAfter(dateTo)) {
                    currentDate = currentDate.minusMonths(months)
                }
                checkAfter = false
            }

            var currentBefore = currentDate
            var currentAfter = currentDate
            while (checkAfter && !currentAfter.isAfter(dateTo)) {
                movements[currentAfter.toString()] = periodicMovement
                currentAfter = currentAfter.plusMonths(months)
            }
            while (checkBefore && !currentBefore.isBefore(dateFrom)) {
                movements[currentBefore.toString()] = periodicMovement
                currentBefore = currentBefore.minusMonths(months)
            }
        }
        // ----------------- WEEKDAYS -----------------
        val weekDays = arrayListOf<DayOfWeek>()
        if (periodicMovement.monday) {
            weekDays.add(DayOfWeek.MONDAY)
        }
        if (periodicMovement.tuesday) {
            weekDays.add(DayOfWeek.TUESDAY)
        }
        if (periodicMovement.wednesday) {
            weekDays.add(DayOfWeek.WEDNESDAY)
        }
        if (periodicMovement.thursday) {
            weekDays.add(DayOfWeek.THURSDAY)
        }
        if (periodicMovement.friday) {
            weekDays.add(DayOfWeek.FRIDAY)
        }
        if (periodicMovement.saturday) {
            weekDays.add(DayOfWeek.SATURDAY)
        }
        if (periodicMovement.sunday) {
            weekDays.add(DayOfWeek.SUNDAY)
        }
        if (weekDays.isNotEmpty()) {
            Log.i(logTag, "Checking weekdays")
            var currentDate = dateTo
            // Move backward to the first weekday to repeat
            while (!weekDays.contains(currentDate.dayOfWeek)) {
                currentDate = currentDate.minusDays(1)
            }

            var k = weekDays.indexOf(currentDate.dayOfWeek)
            val n = weekDays.size
            while (!currentDate.isBefore(dateFrom)) {
                movements[currentDate.toString()] = periodicMovement
                // Move to the next weekday to check, backwards
                // The next day to check is the next in weekDays array
                // Return to first if reached the end of weekDays array
                k = (k + 1) % n
                currentDate = currentDate.with(TemporalAdjusters.previous(weekDays[k]))
            }
        }
        return movements
    }
}