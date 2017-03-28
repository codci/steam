cd test-classes\tools\
del log.log
compare.exe -metric rmse %1 %2 %3 >log.log 2>&1