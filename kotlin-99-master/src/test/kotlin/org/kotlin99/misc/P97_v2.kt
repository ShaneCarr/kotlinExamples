package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.fill
import org.kotlin99.misc.SudokuDancingLinks.Board.Companion.toSudokuBoard
import org.kotlin99.misc.SudokuDancingLinks.ExactCoverMatrix.Companion.toSudokuBoard
import org.kotlin99.misc.dancinglinks.DLMatrix
import org.kotlin99.misc.dancinglinks.Node
import java.util.*

@Suppress("unused") // Because this class is a "namespace".
class SudokuDancingLinks {

    data class Board(private val cells: List<Int>) {

        fun toExactCoverMatrix(): ExactCoverMatrix {
            val matrix = ExactCoverMatrix()
            0.rangeTo(8).forEach { row ->
                0.rangeTo(8).forEach { column ->
                    0.rangeTo(8).forEach { number ->
                        val boardNumber = cells[row * 9 + column]
                        if (boardNumber == 0 || boardNumber == number + 1) {
                            matrix.addConstraintsForMove(row, column, number)
                        }
                    }
                }
            }
            return matrix
        }

        override fun toString(): String {
            fun <T> List<T>.slicedBy(sliceSize: Int): List<List<T>> =
                if (size <= sliceSize) listOf(this)
                else listOf(take(sliceSize)) + drop(sliceSize).slicedBy(sliceSize)

            fun <T> List<T>.mapJoin(separator: String, f: (T) -> String) =
                joinToString(separator) { f(it) }

            return cells.slicedBy(27).mapJoin("\n---+---+---\n") { section ->
                section.slicedBy(9).mapJoin("\n") { row ->
                    row.slicedBy(3).mapJoin("|") { slice ->
                        slice.mapJoin("", Int::toString)
                    }
                }
            }
        }

        companion object {
            fun String.toSudokuBoard(): Board {
                return Board(replace(Regex("[|\\-+\n]"), "").map { c ->
                    if (c == '.') 0 else c.toString().toInt()
                })
            }
        }
    }

    class ExactCoverMatrix(val value: ArrayList<List<Int>> = ArrayList()) {
        fun addConstraintsForMove(row: Int, column: Int, number: Int) {
            val amountOfConstraints = 9 * 9 * 4
            val constraints = ArrayList<Int>().fill(amountOfConstraints, 0)

            val cellConstraintIndex = row * 9 + column
            val rowConstraintIndex = row * 9 + number
            val columnConstraintIndex = column * 9 + number
            val squareConstraintIndex = ((row / 3) * 3 + column / 3) * 9 + number

            constraints[cellConstraintIndex] = 1
            constraints[9 * 9 + rowConstraintIndex] = 1
            constraints[9 * 9 * 2 + columnConstraintIndex] = 1
            constraints[9 * 9 * 3 + squareConstraintIndex] = 1

            value.add(constraints)
        }

        companion object {
            fun List<Node>.toSudokuBoard(): Board {
                val boardCells = ArrayList<Int>().fill(81, 0)
                forEach { node ->
                    val indices = node.toListRight().map { it.header.toString().toInt() }.sorted()
                    val row = indices[0] / 9
                    val column = indices[0] % 9
                    val number = (indices[1] - 9 * 9) % 9
                    boardCells[row * 9 + column] = number + 1
                }
                return Board(boardCells)
            }
        }
    }
}

class SudokuDancingLinksTest {
    @Test fun `convert sudoku board to dancing links matrix`() {
        val matrix = """
            |..4|8..|.17
            |67.|9..|...
            |5.8|.3.|..4
            |---+---+---
            |3..|74.|1..
            |.69|...|78.
            |..1|.69|..5
            |---+---+---
            |1..|.8.|3.6
            |...|..6|.91
            |24.|..1|5..
        """.trimMargin().toSudokuBoard().toExactCoverMatrix()

        assertThat(matrix.value.take(27).joinToString("\n") { it.joinToString("") }, equalTo("""
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000
            |100000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000
            |010000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000
            |001000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000
            |000100000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000
            |000010000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000
        """.trimMargin().trim()))
    }

    @Test fun `solve sudoku example from readme`() {
        val matrix = """
            |..4|8..|.17
            |67.|9..|...
            |5.8|.3.|..4
            |---+---+---
            |3..|74.|1..
            |.69|...|78.
            |..1|.69|..5
            |---+---+---
            |1..|.8.|3.6
            |...|..6|.91
            |24.|..1|5..
        """.trimMargin().toSudokuBoard().toExactCoverMatrix()

        val solution = DLMatrix(matrix.value).search()

        assertThat(solution.toSudokuBoard().toString(), equalTo("""
            |934|825|617
            |672|914|853
            |518|637|924
            |---+---+---
            |325|748|169
            |469|153|782
            |781|269|435
            |---+---+---
            |197|582|346
            |853|476|291
            |246|391|578
        """.trimMargin()))
    }

    @Test fun `solve very hard sudoku from dailysudoku-dot-com`() {
        // http://dailysudoku.com/sudoku/archive/2016/11/2016-11-7_solution.shtml
        val matrix = """
            |.1.|...|..4
            |7.9|..1|..5
            |..5|9.7|..6
            |---+---+---
            |3.4|...|.2.
            |...|3.2|...
            |.7.|...|9.3
            |---+---+---
            |9..|8.6|4..
            |2..|5..|3.1
            |1..|...|.6.
        """.trimMargin().toSudokuBoard().toExactCoverMatrix()

        val solution = DLMatrix(matrix.value).search()

        assertThat(solution.toSudokuBoard().toString(), equalTo("""
            |816|235|794
            |729|461|835
            |435|987|216
            |---+---+---
            |384|659|127
            |591|372|648
            |672|148|953
            |---+---+---
            |953|816|472
            |267|594|381
            |148|723|569
        """.trimMargin()))
    }

}