// GLPixelFont provides meshes to render basic ASCII characters in OpenGL.
// The font is fixed-width 5x7, and contains [0,9], uppercase [A,Z] and - . : = ?
// Ulf Benjaminsson, 2021-10-15
package com.gmail.erikephggustafsson.glasteroids

import android.opengl.GLES20
import com.gmail.erikephggustafsson.glasteroids.COORDS_PER_VERTEX
import com.gmail.erikephggustafsson.glasteroids.Mesh
import java.util.*

const val GLYPH_SPACING = 1f;
const val GLYPH_WIDTH = 5 //characters are 5 units wide
const val GLYPH_HEIGHT = 7 //characters are 7 units tall
private const val CHAR_COUNT = 46 //the font definition contains 45 entries
private const val CHAR_OFFSET = 45 //it start at ASCII code 45 "-", and ends at 90 "Z".

//FONT_DEFINITION contains most of basic ASCII characters 45-90:
//Specifically: [0,9], uppercase [A,Z] and - . : = ?
private const val FONT_DEFINITION =
        /*[ind asc    sym]*/
    /*[0   45 '-']*/     "00000" + "00000" + "00000" + "11111" + "00000" + "00000" + "00000" + //-
        /*[1   46 '.']*/     "00000" + "00000" + "00000" + "00000" + "00000" + "01100" + "01100" + //.
        /*[2   47 '/']*/     "00001" + "00001" + "00010" + "00100" + "00100" + "01000" + "10000" + //
        /*[3   48 '0']*/     "01110" + "10001" + "10011" + "10101" + "11001" + "10001" + "01110" + //0
        /*[4   49 '1']*/     "00100" + "01100" + "00100" + "00100" + "00100" + "00100" + "01110" + //1
        /*[5   50 '2']*/     "01110" + "10001" + "00001" + "00010" + "00100" + "01000" + "11111" + //2
        /*[6   51 '3']*/     "01110" + "10001" + "00001" + "00110" + "00001" + "10001" + "01110" + //3
        /*[7   52 '4']*/     "00010" + "00110" + "01010" + "10010" + "11111" + "00010" + "00111" + //4
        /*[8   53 '5']*/     "11111" + "10000" + "11110" + "00001" + "00001" + "10001" + "01110" + //5
        /*[9   54 '6']*/     "01110" + "10001" + "10000" + "11110" + "10001" + "10001" + "01110" + //6
        /*[10  55 '7']*/     "11111" + "10001" + "00010" + "00010" + "00100" + "00100" + "00100" + //7
        /*[11  56 '8']*/     "01110" + "10001" + "10001" + "01110" + "10001" + "10001" + "01110" + //8
        /*[12  57 '9']*/     "01110" + "10001" + "10001" + "01111" + "00001" + "00001" + "01110" + //9
        /*[13  58 ':']*/     "00000" + "01100" + "01100" + "00000" + "01100" + "01100" + "00000" + //:
        /*[14  59 ';']*/     "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + //
        /*[15  60 '<']*/     "00011" + "00100" + "01000" + "10000" + "01000" + "00100" + "00011" + //
        /*[16  61 '=']*/     "00000" + "00000" + "11111" + "00000" + "11111" + "00000" + "00000" + //=
        /*[17  62 '>']*/     "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + //
        /*[18  63 '?']*/     "01110" + "10001" + "10001" + "00010" + "00100" + "00000" + "00100" + //?
        /*[19  64 '@']*/     "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + "11111" + //
        /*[20  65 'A']*/     "01110" + "10001" + "10001" + "11111" + "10001" + "10001" + "10001" + //A
        /*[21  66 'B']*/     "11110" + "10001" + "10001" + "11110" + "10001" + "10001" + "11110" + //B
        /*[22  67 'C']*/     "01110" + "10001" + "10000" + "10000" + "10000" + "10001" + "01110" + //C
        /*[23  68 'D']*/     "11110" + "10001" + "10001" + "10001" + "10001" + "10001" + "11110" + //D
        /*[24  69 'E']*/     "11111" + "10000" + "10000" + "11110" + "10000" + "10000" + "11111" + //E
        /*[25  70 'F']*/     "11111" + "10000" + "10000" + "11110" + "10000" + "10000" + "10000" + //F
        /*[26  71 'G']*/     "01110" + "10001" + "10000" + "10111" + "10001" + "10001" + "01110" + //G
        /*[27  72 'H']*/     "10001" + "10001" + "10001" + "11111" + "10001" + "10001" + "10001" + //H
        /*[28  73 'I']*/     "01110" + "00100" + "00100" + "00100" + "00100" + "00100" + "01110" + //I
        /*[29  74 'J']*/     "00001" + "00001" + "00001" + "00001" + "10001" + "10001" + "01110" + //J
        /*[30  75 'K']*/     "10001" + "10010" + "10100" + "11000" + "10100" + "10010" + "10001" + //K
        /*[31  76 'L']*/     "10000" + "10000" + "10000" + "10000" + "10000" + "10000" + "11111" + //L
        /*[32  77 'M']*/     "10001" + "11011" + "10101" + "10101" + "10001" + "10001" + "10001" + //M
        /*[33  78 'N']*/     "10001" + "10001" + "11001" + "10101" + "10011" + "10001" + "10001" + //N
        /*[34  79 'O']*/     "01110" + "10001" + "10001" + "10001" + "10001" + "10001" + "01110" + //O
        /*[35  80 'P']*/     "11110" + "10001" + "10001" + "11110" + "10000" + "10000" + "10000" + //P
        /*[36  81 'Q']*/     "01110" + "10001" + "10001" + "10001" + "10101" + "10010" + "01101" + //Q
        /*[37  82 'R']*/     "11110" + "10001" + "10001" + "11110" + "10100" + "10010" + "10001" + //R
        /*[38  83 'S']*/     "01111" + "10000" + "10000" + "01110" + "00001" + "00001" + "11110" + //S
        /*[39  84 'T']*/     "11111" + "00100" + "00100" + "00100" + "00100" + "00100" + "00100" + //T
        /*[40  85 'U']*/     "10001" + "10001" + "10001" + "10001" + "10001" + "10001" + "01110" + //U
        /*[41  86 'V']*/     "10001" + "10001" + "10001" + "10001" + "10001" + "01010" + "00100" + //V
        /*[42  87 'W']*/     "10001" + "10001" + "10001" + "10101" + "10101" + "10101" + "01010" + //W
        /*[43  88 'X']*/     "10001" + "10001" + "01010" + "00100" + "01010" + "10001" + "10001" + //X
        /*[44  89 'Y']*/     "10001" + "10001" + "10001" + "01010" + "00100" + "00100" + "00100" + //Y
        /*[45  90 'Z']*/     "11111" + "00001" + "00010" + "00100" + "01000" + "10000" + "11111"  //Z


val BLANK_SPACE = Mesh(floatArrayOf(0.0f), GLES20.GL_POINTS)
object GLPixelFont {
    private val _glyphs = ArrayList<Mesh>(CHAR_COUNT) //a vertex buffer for each glyph, for rendering with OpenGL.

    init {
        for (c in 45..90) {
            _glyphs.add(createMeshForGlyph(c.toChar()))
        }
    }

    fun getString(s: String): ArrayList<Mesh> {
        val count = s.length
        val result = ArrayList<Mesh>(count)
        for (i in 0 until count) {
            val c = s[i]
            val m = getChar(c)
            result.add(m)
        }
        return result
    }

    fun getChar(c: Char): Mesh {
        var C = Character.toUpperCase(c)
        if(C.code < CHAR_OFFSET || c.code >= CHAR_OFFSET + CHAR_COUNT){
            return BLANK_SPACE;
        }
        //assert(C.code > CHAR_OFFSET && C.code <= CHAR_OFFSET + CHAR_COUNT)
        val i = C.code - CHAR_OFFSET
        return _glyphs[i]
    }

    private fun createMeshForGlyph(c: Char): Mesh {
        assert(c.code >= CHAR_OFFSET && c.code < CHAR_OFFSET + CHAR_COUNT)
        val vertices = FloatArray(GLYPH_HEIGHT * GLYPH_WIDTH * COORDS_PER_VERTEX)
        val z = 0f
        val charIndex = c.code - CHAR_OFFSET
        var i = 0
        for (y in 0 until GLYPH_HEIGHT) {
            for (x in 0 until GLYPH_WIDTH) {
                val index = GLYPH_HEIGHT * GLYPH_WIDTH * charIndex + GLYPH_WIDTH * y + x
                if (FONT_DEFINITION[index] == '0') {
                    continue
                }
                vertices[i++] = x.toFloat()
                vertices[i++] = y.toFloat()
                vertices[i++] = z
            }
        }
        val clean: FloatArray = Arrays.copyOfRange(vertices, 0, i)
        return Mesh(clean, GLES20.GL_POINTS)
    }
}