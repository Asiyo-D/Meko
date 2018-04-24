package com.loqoursys.meko.data

/**
 * Created by root on 4/22/18 for LoqourSys
 */

data class SavedLocations(var home: Home = Home(), var work: Work = Work())


data class Home(var latitude: Double = 0.0, var longitutde: Double = 0.0,
                var location_name: String = "", var description: String = "")

data class Work(var latitude: Double = 0.0, var longitutde: Double = 0.0,
                var location_name: String = "", var description: String = "")