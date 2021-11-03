package com.lepu.blepro.ble.data

class Bp2FilePart {
    var name : String
    var fileSize : Int
    var curSize : Int
    var percent : Float

    constructor(name : String, fileSize : Int, curSize : Int) {
        this.name = name
        this.fileSize =  fileSize
        this.curSize = curSize
        this.percent = curSize/fileSize.toFloat()
    }

    override fun toString(): String {

        val string = """
            
            download file: $name
            total: $fileSize; current: $curSize
            percent: $percent
        """
        return string
    }
}