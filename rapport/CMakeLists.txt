cmake_minimum_required(VERSION 2.9)

project(latex-template NONE)

SET(LATEX_DEFAULT_BUILD pdf)
SET(LATEX_OUTPUT_PATH build)

INCLUDE(includes/UseLATEX.cmake)

FILE(GLOB_RECURSE LATEX_DOCUMENT_INPUTS RELATIVE ${CMAKE_SOURCE_DIR} source/**/*.tex source/**/*.sty)

# UseLATEX.cmake documentation can be found here: https://gitlab.kitware.com/kmorel/UseLATEX
ADD_LATEX_DOCUMENT(source/main.tex
    INPUTS ${LATEX_DOCUMENT_INPUTS}
)

ADD_CUSTOM_COMMAND(TARGET main_pdf POST_BUILD
                   COMMAND ${CMAKE_COMMAND} -E copy
                       ${CMAKE_CURRENT_BINARY_DIR}/main.pdf
                       ${CMAKE_BINARY_DIR}/../main.pdf)
