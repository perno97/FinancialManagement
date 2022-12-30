package com.perno97.financialmanagement.database

import android.util.Log
import com.perno97.financialmanagement.viewmodels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object PeriodicMovementsChecker {
    private const val logTag = "PeriodicMovementsChecker"

    fun check( // TODO creare incumbent nelle prossime x settimane
        appViewModel: AppViewModel,
        scope: CoroutineScope,
        callback: (() -> Unit)?
    ) {
        Log.e(logTag, "Called check")
        scope.launch {
            val list = appViewModel.getAllPeriodicMovements()
            for (periodicMovement in list) {
                val movement = appViewModel.getLatestPeriodicMovement(
                    periodicMovementId = periodicMovement.periodicMovementId,
                    dateTo = LocalDate.now(),
                    dateFrom = periodicMovement.date
                )
                Log.e(logTag, "Movement $movement")
                val calculatedMovements = if (movement != null) {
                    getMovementsFromPeriodicMovement(
                        periodicMovement,
                        movement.date.plusDays(1),
                        LocalDate.now()
                    )
                } else {
                    getMovementsFromPeriodicMovement(
                        periodicMovement,
                        periodicMovement.date,
                        LocalDate.now()
                    )
                }
                var amountsSum = 0f
                for (date in calculatedMovements.keys) {
                    val mov: PeriodicMovement = calculatedMovements[date]!!
                    val amount = mov.amount
                    appViewModel.insert(
                        Movement(
                            date = LocalDate.parse(date),
                            amount = amount,
                            category = mov.category,
                            title = mov.title,
                            notes = mov.notes,
                            periodicMovementId = periodicMovement.periodicMovementId
                        )
                    )
                    amountsSum += amount
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
                currentDate = currentDate.with(TemporalAdjusters.previousOrSame(weekDays[k]))
            }
        }
        return movements
    }
}