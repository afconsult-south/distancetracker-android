package com.afconsult.korjournal

import java.util.*

class TripObject {

    /// Trip properties, corresponding to the properties stored in the persisting context.

    var traveller   : String = "Adrian"

    var startingLocation: String? = null
    var destination: String? = null

    var distance: Double = 0.toDouble()

    var startDate: Date? = null
    var endDate: Date? = null

    var path: android.util.Pair<Double, Double>? = null

    var isExported = false



//    private var uuid      : UUID

    /// Formatters for formatted strings.
//    private let dateFormatter          = DateFormatter()
//    private let dateComponentFormatter = DateComponentsFormatter()
//    private let measurementFormatter   = MeasurementFormatter()

    /// Formatted strings.
//    public var startDateString      : String { return extractDate(from: startDate)}
//    public var startTimeString      : String { return extractTime(from: startDate)}
//    public var endTimeString        : String { return extractTime(from: endDate)}

    init {

    }

}