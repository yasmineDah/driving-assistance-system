package app.data.model

class LocationHelper(alt: Double = 0.0, long: Double= 0.0) {

    private var alt = alt
    private var long = long

    fun getAlt(): Double {
        return alt
    }

    fun setAlt(alt: Double) {
        this.alt = alt
    }

    fun getLong(): Double {
        return long
    }

    fun setLong(long: Double) {
        this.long = long
    }


}
