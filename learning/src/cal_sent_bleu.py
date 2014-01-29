from simpbleu import Obtain_bleu
from itertools import izip
#from __future__ import print_function

def service_func():
    print 'service func'

if __name__ == '__main__':
    # service.py executed as script
    # do something
    #service_func()
    source = open('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt13qe/test_system.spa','r').readlines()
    target = open('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt13qe/test_reference.tex','r').readlines()
    #source = open('/Users/kashif/Documents/projects/quest-master/input/target.es','r').readlines()
    #target = open('/Users/kashif/Documents/projects/quest-master/learning/training_set_annotations/target_reference.spa','r').readlines()

    #output = open('/Users/kashif/Documents/projects/quest-master/learning/data/features/wmt2012_qe_baseline/fr-en/bleu_score','w')
    output = open('/Users/kashif/Documents/projects/quest-master/learning/bleu_score','w')
    for src,tgt in izip(source,target):
	bleu, length = Obtain_bleu(source,target).bleu_sentence(src, tgt)
	#output.write(str(bleu))
	print bleu*100
	print >> output, bleu
    output.close()
