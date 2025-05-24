package GameHandler

import java.io.File

object MapLoader {
    fun loadMaps(path: String): List<Map>{
        val folder = File(path)
        if (!folder.exists()){
            println("Ошибка, папка $path не найдена")
            return emptyList()
        }
        return folder.listFiles {file -> file.extension == "map"}?.mapNotNull { file -> try {
            val content = file.readText()
            Map.deserialize(content)
        } catch (e: Exception){
            println("Ошибка")
            null
        }
        }?: emptyList()
    }

    fun loadMapNames(path: String): MutableList<String>{
        var res = mutableListOf<String>()
        val folder = File(path)
        folder.listFiles{file -> file.extension == "map"}.forEach{ file ->
            res.add(file.nameWithoutExtension)
        }
        return res
    }
}
