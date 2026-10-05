package com.abrarshakhi.lumen.provider.units

/** A family of units that can be converted between one another. */
enum class Dimension(val displayName: String) {
    Length("Length"),
    Mass("Mass"),
    Temperature("Temperature"),
    Data("Data"),
    Time("Time"),
    Speed("Speed"),
    Area("Area"),
    Volume("Volume"),
}

/**
 * A unit, expressed as an affine transform onto its dimension's base unit.
 *
 * `base = value * toBase + offset`
 *
 * The offset exists for temperature, which is the one dimension where conversion is not a
 * pure ratio — treating Fahrenheit as a simple scale factor is the classic way to get
 * plausible-looking but wrong answers.
 */
data class MeasurementUnit(
    val id: String,
    val dimension: Dimension,
    val symbol: String,
    val aliases: Set<String>,
    val toBase: Double,
    val offset: Double = 0.0,
) {
    fun toBaseValue(value: Double): Double = value * toBase + offset
    fun fromBaseValue(base: Double): Double = (base - offset) / toBase
}

/**
 * Every unit Lumen understands.
 *
 * Aliases are unique across the whole registry, not just within a dimension — an ambiguous
 * alias would make conversion depend on declaration order, which is untraceable for the
 * user. A test enforces this.
 */
object UnitRegistry {

    val units: List<MeasurementUnit> = buildList {
        // --- Length (base: metre) ---
        add(unit("mm", Dimension.Length, "mm", 0.001, "mm", "millimetre", "millimeter", "millimetres", "millimeters"))
        add(unit("cm", Dimension.Length, "cm", 0.01, "cm", "centimetre", "centimeter", "centimetres", "centimeters"))
        add(unit("m", Dimension.Length, "m", 1.0, "m", "metre", "meter", "metres", "meters"))
        add(unit("km", Dimension.Length, "km", 1000.0, "km", "kilometre", "kilometer", "kilometres", "kilometers"))
        add(unit("in", Dimension.Length, "in", 0.0254, "in", "inch", "inches", "\""))
        add(unit("ft", Dimension.Length, "ft", 0.3048, "ft", "foot", "feet"))
        add(unit("yd", Dimension.Length, "yd", 0.9144, "yd", "yard", "yards"))
        add(unit("mi", Dimension.Length, "mi", 1609.344, "mi", "mile", "miles"))
        add(unit("nmi", Dimension.Length, "nmi", 1852.0, "nmi", "nauticalmile", "nauticalmiles"))

        // --- Mass (base: kilogram) ---
        add(unit("mg", Dimension.Mass, "mg", 0.000001, "mg", "milligram", "milligrams"))
        add(unit("g", Dimension.Mass, "g", 0.001, "g", "gram", "grams"))
        add(unit("kg", Dimension.Mass, "kg", 1.0, "kg", "kilo", "kilos", "kilogram", "kilograms"))
        add(unit("tonne", Dimension.Mass, "t", 1000.0, "tonne", "tonnes", "metricton"))
        add(unit("oz", Dimension.Mass, "oz", 0.028349523125, "oz", "ounce", "ounces"))
        add(unit("lb", Dimension.Mass, "lb", 0.45359237, "lb", "lbs", "pound", "pounds"))
        add(unit("st", Dimension.Mass, "st", 6.35029318, "st", "stone", "stones"))

        // --- Temperature (base: Celsius; affine, see MeasurementUnit) ---
        add(unit("c", Dimension.Temperature, "°C", 1.0, "c", "°c", "celsius", "centigrade"))
        add(
            MeasurementUnit(
                id = "f",
                dimension = Dimension.Temperature,
                symbol = "°F",
                aliases = setOf("f", "°f", "fahrenheit"),
                toBase = 5.0 / 9.0,
                offset = -32.0 * 5.0 / 9.0,
            ),
        )
        add(
            MeasurementUnit(
                id = "k",
                dimension = Dimension.Temperature,
                symbol = "K",
                aliases = setOf("k", "kelvin"),
                toBase = 1.0,
                offset = -273.15,
            ),
        )

        // --- Data (base: byte; decimal and binary kept distinct) ---
        add(unit("bit", Dimension.Data, "bit", 0.125, "bit", "bits"))
        add(unit("byte", Dimension.Data, "B", 1.0, "byte", "bytes"))
        add(unit("kb", Dimension.Data, "kB", 1_000.0, "kb", "kilobyte", "kilobytes"))
        add(unit("mb", Dimension.Data, "MB", 1_000_000.0, "mb", "megabyte", "megabytes"))
        add(unit("gb", Dimension.Data, "GB", 1_000_000_000.0, "gb", "gigabyte", "gigabytes"))
        add(unit("tb", Dimension.Data, "TB", 1_000_000_000_000.0, "tb", "terabyte", "terabytes"))
        add(unit("kib", Dimension.Data, "KiB", 1024.0, "kib", "kibibyte", "kibibytes"))
        add(unit("mib", Dimension.Data, "MiB", 1048576.0, "mib", "mebibyte", "mebibytes"))
        add(unit("gib", Dimension.Data, "GiB", 1073741824.0, "gib", "gibibyte", "gibibytes"))
        add(unit("tib", Dimension.Data, "TiB", 1099511627776.0, "tib", "tebibyte", "tebibytes"))

        // --- Time (base: second) ---
        add(unit("ms", Dimension.Time, "ms", 0.001, "ms", "millisecond", "milliseconds"))
        add(unit("sec", Dimension.Time, "s", 1.0, "s", "sec", "secs", "second", "seconds"))
        add(unit("min", Dimension.Time, "min", 60.0, "min", "mins", "minute", "minutes"))
        add(unit("hr", Dimension.Time, "h", 3600.0, "h", "hr", "hrs", "hour", "hours"))
        add(unit("day", Dimension.Time, "d", 86400.0, "d", "day", "days"))
        add(unit("week", Dimension.Time, "wk", 604800.0, "wk", "week", "weeks"))
        add(unit("year", Dimension.Time, "yr", 31557600.0, "yr", "year", "years"))

        // --- Speed (base: metres per second) ---
        add(unit("mps", Dimension.Speed, "m/s", 1.0, "m/s", "mps"))
        add(unit("kph", Dimension.Speed, "km/h", 1.0 / 3.6, "km/h", "kmh", "kph"))
        add(unit("mph", Dimension.Speed, "mph", 0.44704, "mph", "mi/h"))
        add(unit("knot", Dimension.Speed, "kn", 0.514444, "kn", "knot", "knots"))

        // --- Area (base: square metre) ---
        add(unit("m2", Dimension.Area, "m²", 1.0, "m2", "m²", "sqm"))
        add(unit("km2", Dimension.Area, "km²", 1_000_000.0, "km2", "km²", "sqkm"))
        add(unit("ft2", Dimension.Area, "ft²", 0.09290304, "ft2", "ft²", "sqft"))
        add(unit("acre", Dimension.Area, "acre", 4046.8564224, "acre", "acres"))
        add(unit("hectare", Dimension.Area, "ha", 10000.0, "ha", "hectare", "hectares"))

        // --- Volume (base: litre) ---
        add(unit("ml", Dimension.Volume, "ml", 0.001, "ml", "millilitre", "milliliter", "millilitres", "milliliters"))
        add(unit("l", Dimension.Volume, "l", 1.0, "l", "litre", "liter", "litres", "liters"))
        add(unit("gal", Dimension.Volume, "gal", 3.785411784, "gal", "gallon", "gallons"))
        add(unit("qt", Dimension.Volume, "qt", 0.946352946, "qt", "quart", "quarts"))
        add(unit("pt", Dimension.Volume, "pt", 0.473176473, "pt", "pint", "pints"))
        add(unit("floz", Dimension.Volume, "fl oz", 0.0295735295625, "floz", "fluidounce", "fluidounces"))
        add(unit("cup", Dimension.Volume, "cup", 0.2365882365, "cup", "cups"))
    }

    private val byAlias: Map<String, MeasurementUnit> =
        units.flatMap { unit -> unit.aliases.map { it to unit } }.toMap()

    fun find(alias: String): MeasurementUnit? = byAlias[alias.lowercase().trim()]

    private fun unit(
        id: String,
        dimension: Dimension,
        symbol: String,
        toBase: Double,
        vararg aliases: String,
    ) = MeasurementUnit(id, dimension, symbol, aliases.toSet(), toBase)
}
