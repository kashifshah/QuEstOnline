import pylab as pb
import numpy as np
import GPy
import scipy as sc
pb.ion()
pb.close('all')

X = np.genfromtxt('test_all-smt1.gb_features', delimiter=',')
#print X
Y = np.genfromtxt('index_feat_17', dtype=int) # .reshape(-1,1)
#print Y

#selected = sort_ls[0:i];
test = X[:,Y-1];
#test = X[Y-1,:];
#train = sc.delete(X,Y-1,0)
#test.tofile('test2.out', sep=",")
np.savetxt('test_17-smt1.gb_features', test, delimiter=',') #, newline='\n')
#np.savetxt('train_soton_all.out', train, delimiter=',') #, newline='\n')
#np.save('outfile', test)
print test

