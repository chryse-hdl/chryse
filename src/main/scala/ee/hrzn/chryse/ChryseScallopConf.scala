/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.Subcommand

// TODO (Scallop): Show parent version string on subcommand help.
private[chryse] class ChryseScallopConf(chryse: ChryseApp, args: Array[String])
    extends ScallopConf(args) {
  private val appVersion =
    chryse.getClass().getPackage().getImplementationVersion()
  val versionBanner = s"${chryse.name} $appVersion (Chryse " +
    s"${ChryseApp.getChrysePackage().getImplementationVersion()})"

  var terminating = false

  if (System.getenv().getOrDefault("CHRYSE_APP_NOEXIT", "") == "1")
    exitHandler = _ => terminating = true
  printedName = chryse.name

  version(versionBanner)

  object build extends Subcommand("build") {
    val onto =
      if (chryse.targetPlatforms.length > 1) ""
      else s" onto ${chryse.targetPlatforms(0).id}"
    banner(s"Build the design$onto, and optionally program it.")

    val board =
      if (chryse.targetPlatforms.length > 1)
        Some(
          choice(
            chryse.targetPlatforms.map(_.id),
            name = "board",
            argName = "board",
            descr = s"Board to build for.", // + " Choices: ..."
            required = true,
          ),
        )
      else None

    val program =
      opt[Boolean](
        descr = "Program the design onto the board after building",
      )

    val programMode =
      if (chryse.targetPlatforms.exists(_.programmingModes.length > 1))
        Some(
          opt[String](
            name = "program-mode",
            short = 'm',
            descr = "Alternate programming mode (use -m ? with a board specified to list)",
          ),
        )
      else None

    if (board.isDefined && programMode.isDefined)
      validateOpt(board.get, programMode.get) {
        case (Some(b), Some(pm)) if pm != "?" =>
          val plat = chryse.targetPlatforms.find(_.id == b).get
          if (plat.programmingModes.exists(_._1 == pm))
            Right(())
          else
            Left("Invalid programming mode (use -m ? to list)")
        case _ => Right(())
      }

    val fullStacktrace = opt[Boolean](
      short = 'F',
      descr = "Include full Chisel stacktraces",
    )
  }
  addSubcommand(build)

  object cxxrtl extends Subcommand("cxxrtl") {
    banner("Run the CXXRTL simulator tests.")

    val platform =
      if (chryse.cxxrtlPlatforms.length > 1)
        Some(
          choice(
            chryse.cxxrtlPlatforms.map(_.id),
            name = "platform",
            argName = "platform",
            descr = "CXXRTL platform to use.",
            required = true,
          ),
        )
      else
        None
    val force =
      opt[Boolean](
        descr = "Clean before build",
      )
    val compileOnly =
      opt[Boolean](
        name = "compile",
        descr = "Compile only; don't run",
      )
    val optimize =
      opt[Boolean](
        short = 'O',
        descr = "Build with optimizations",
      )
    val debug = opt[Boolean](
      descr = "Generate source-level debug information",
    )
    val vcd =
      opt[String](
        argName = "file",
        descr = "Output a VCD file when running simulation (passes --vcd <file> to the simulation executable)",
      )
    val trailing = trailArg[List[String]](
      name = "<arg> ...",
      descr = "Other arguments for the simulation executable",
      required = false,
    )
  }
  if (chryse.cxxrtlPlatforms.nonEmpty)
    addSubcommand(cxxrtl)

  for { sc <- chryse.additionalSubcommands }
    addSubcommand(sc)
}
