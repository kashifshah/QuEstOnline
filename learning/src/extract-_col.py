import pylab as pb
import numpy as np
import GPy
import scipy as sc
pb.ion()
pb.close('all')

X = np.genfromtxt('test.out', delimiter=',')
#print X
Y = np.genfromtxt('index_17_feat', dtype=int) # .reshape(-1,1)
#print Y

#selected = sort_ls[0:i];
test = X[:,Y-1];
#train = sc.delete(X,Y-1,0)
#test.tofile('test2.out', sep=",")
np.savetxt('test17.out', test, delimiter=',') #, newline='\n')
#np.save('outfile', test)
print test

