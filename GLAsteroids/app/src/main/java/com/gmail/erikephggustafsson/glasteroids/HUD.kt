package com.gmail.erikephggustafsson.glasteroids

class HUD() {
    private val listOfTexts = ArrayList<Text>()

    fun addTextElement(x: Float, y: Float):Int{
        val text = Text("",x,y)
        listOfTexts.add(text)
        return listOfTexts.size-1
    }

    fun updateDisplayText(index: Int, displayText: String){
        listOfTexts[index].setString(displayText)
    }

    //not the most useful class, but at least it manage to clean up render in game
    //and don't require to constantly update every value every frame, each update
    //can be called by the holder of the displayed value when a change is made
    fun render(viewportMatrix: FloatArray){
        for(text in listOfTexts){
            text.render(viewportMatrix)
        }
    }
}