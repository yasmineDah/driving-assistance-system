package app.data.model

import com.google.android.gms.maps.model.LatLng
import java.util.*


class Danger(Location: LocationHelper = LocationHelper(0.0,0.0), Number: Int = -1, Type: String = "", DateCreated: Long = -1) {

    private var location: LocationHelper = Location
    private var number: Int = Number
    private var type: String = Type
    private var dateCreated: Long = DateCreated


    fun getType(): String {
        return type
    }

    fun setType(Type: String) {
        type = Type
    }

    fun getNumber(): Int {
        return number
    }

    fun setNumber(Number: Int) {
        number = Number
    }

    fun getLocation(): LocationHelper {
        return location
    }

    fun setLocation(Location: LocationHelper){
        location = Location
    }

    fun getDateCreated(): Long {
        return dateCreated
    }

    fun setDateCreated(date: Long) {
        dateCreated = date
    }

}