"""
Simple Gaussian Processes regression with an RBF kernel
"""
import pylab as pb
import numpy as np
import GPy
pb.ion()
pb.close('all')

#X = np.genfromtxt('../corrected_encod_v2/train-79-features.qe.tsv')
#test_X = np.genfromtxt('../corrected_encod_v2/test-79-features.qe.tsv')
#Y = np.genfromtxt('../corrected_encod_v2/qe_reference_en-es.train.effort').reshape(-1, 1)
#test_Y = np.genfromtxt('../corrected_encod_v2/qe_reference_en-es.test.effort').reshape(-1, 1)

X = np.genfromtxt('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt2012_qe_baseline/train-79-features.qe.tsv')
test_X = np.genfromtxt('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt2012_qe_baseline/test-79-features.qe.tsv')
Y = np.genfromtxt('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt2012_qe_baseline/training.effort').reshape(-1, 1)
test_Y = np.genfromtxt('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt2012_qe_baseline/test.effort').reshape(-1, 1)



# rescale X
mx = np.mean(X,axis=0)
sx = np.std(test_X,axis=0)

ok = (sx > 0)
X = X[:,ok]
test_X = test_X[:,ok]
sx, mx = sx[ok], mx[ok]

X = (X - mx) / sx
test_X = (test_X - mx) / sx

print 'Dropped features with constant values:'
print np.nonzero(ok == False)[0]

# we could centre Y too?

D = X.shape[1]

# this is as big as I can go on my laptop :)
if True:
    X = X[:1200,:]
    Y = Y[:1200,:]

# construct kernel
rbf = GPy.kern.rbf(D, ARD=True)
noise = GPy.kern.white(D)
kernel = rbf + noise

# create simple GP model
m = GPy.models.GP_regression(X,Y, kernel = kernel)
m.tie_params('lengthscale')
m.constrain_positive('')
m.optimize(max_f_eval = 50, messages = True)
print m

mu, s2, lb, ub = m.predict(test_X)
mae = np.mean(np.abs(mu - test_Y))
rmse = np.mean((mu - test_Y) ** 2) ** 0.5

print 'SEiso -- mae', mae, 'rmse', rmse

# the iso kernel initialises the ARD one to avoid local minima
m.untie_everything()
m.optimize(max_f_eval = 100, messages = True)
print m

mu, s2, lb, ub = m.predict(test_X)
mae = np.mean(np.abs(mu - test_Y))
rmse = np.mean((mu - test_Y) ** 2) ** 0.5

print 'SEard -- mae', mae, 'rmse', rmse

length_scales = m.get('lengthscale')
sort_ls = np.argsort(length_scales)

print 'Feature ranking by length scale'
print sort_ls

for i in range(5, D, 5):
#for i = 5:5:(D) 
   # now take the top i features
   selected = sort_ls[0:i];
   print selected
   train_X_sel = X[:, selected];
   test_X_sel = test_X[:, selected];

   print 'Fitting kernel hyper-parameters selecting %d features\n,' , selected
       # construct kernel
   D = train_X_sel.shape[1]
   rbf = GPy.kern.rbf(D, ARD=True)
   noise = GPy.kern.white(D)
   kernel = rbf + noise

   m = GPy.models.GP_regression(train_X_sel,Y, kernel = kernel)
   m.tie_params('lengthscale')
   m.constrain_positive('')
   m.optimize(max_f_eval = 50, messages = True)
   print m

   mu, s2, lb, ub = m.predict(test_X_sel)
   mae = np.mean(np.abs(mu - test_Y))
   rmse = np.mean((mu - test_Y) ** 2) ** 0.5

   print 'SEiso -- mae', mae, 'rmse', rmse

# the iso kernel initialises the ARD one to avoid local minima
   m.untie_everything()
   m.optimize(max_f_eval = 100, messages = True)
   print m

   mu, s2, lb, ub = m.predict(test_X_sel)
   mae = np.mean(np.abs(mu - test_Y))
   rmse = np.mean((mu - test_Y) ** 2) ** 0.5

   print 'SEard -- mae', mae, 'rmse', rmse
