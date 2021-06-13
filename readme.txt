"SoGen" directory:
    The sources of SoGen tool.
    Use command "java -jar PDLSolver.jar -i <PDL model>" to generate a solver for the given constraint model.




"models-data" directory:
    The models, generated solvers, and data generators of all problems (P1-P9, Q1-Q3) used in our experiments.
    *.pdl: a PDL model
    *.mzn: a MiniZinc model
    *.eprime: an Essence' model
    a.c: a generated solver
    "gen.cpp" or "gen": a data generator