import pylab as pb
import numpy as np
import GPy
import scipy as sc
pb.ion()
pb.close('all')

X = np.genfromtxt('scores/en-es_soton_scores.txt', delimiter=',')
#print X
Y = np.genfromtxt('index_test_samples', dtype=int) # .reshape(-1,1)
#print Y

#selected = sort_ls[0:i];
#test = X[:,Y-1];
test = X[Y-1,:];
train = sc.delete(X,Y-1,0)
#test.tofile('test2.out', sep=",")
np.savetxt('test_soton_effort', test, delimiter=',') #, newline='\n')
np.savetxt('train_soton_effort', train, delimiter=',') #, newline='\n')
#np.save('outfile', test)
#print test

