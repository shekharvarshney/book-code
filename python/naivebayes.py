#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Created on Mon Mar 28 16:56:06 2016

@author: shekhar
"""

import MySQLdb as mdb
import sys

Y='Y'
N='N'
sess_dict={}
instr_dict={}
dir_dict={}
sess_dict[Y]={}
sess_dict[N]={}
instr_dict[Y]={}
instr_dict[N]={}
dir_dict[Y]={}
dir_dict[N]={}

def pSession(sess):
    allctr=reduce(lambda x,y:x+y, sess_dict[Y].values())+reduce(lambda x,y:x+y, sess_dict[N].values())
    
    yctr=0
    nctr=0
    if sess in sess_dict[Y]:
        yctr=sess_dict[Y][sess]
        
    if sess in sess_dict[N]:
        nctr=sess_dict[N][sess]
    return (yctr+nctr)/(1.0*allctr)


def pInstrument(instr):
    allctr=reduce(lambda x,y:x+y, instr_dict[Y].values())+reduce(lambda x,y:x+y, instr_dict[N].values())
    yctr=0
    nctr=0
    if instr in instr_dict[Y]:
        yctr=instr_dict[Y][instr]
    if instr in instr_dict[N]:
        nctr=instr_dict[N][instr]        
    return (yctr+nctr)/(1.0*allctr)
    
def pDirection(direction):
    allctr=reduce(lambda x,y:x+y, dir_dict[Y].values())+reduce(lambda x,y:x+y, dir_dict[N].values())
    yctr=0
    nctr=0
    if direction in dir_dict[Y]:
        yctr=dir_dict[Y][direction]
    if direction in dir_dict[N]:
        nctr=dir_dict[N][direction]
    return (yctr+nctr)/(1.0*allctr)
    
def pBad(isbad):
    allctr=reduce(lambda x,y:x+y, sess_dict[Y].values())+reduce(lambda x,y:x+y, sess_dict[N].values())
    return reduce(lambda x,y:x+y, sess_dict[isbad].values())/(1.0*allctr)
    
def pSessionBad(sess,isbad):
    allctr=reduce(lambda x,y:x+y, sess_dict[isbad].values())
    return sess_dict[isbad][sess]/(1.0*allctr)
    
def pInstrumentBad(instr,isbad):
    allctr=reduce(lambda x,y:x+y, instr_dict[isbad].values())
    if instr in instr_dict[isbad]:
        return instr_dict[isbad][instr]/(1.0*allctr)
    else:
        return 0
        
def pDirectionBad(direction,isbad):
    allctr=reduce(lambda x,y:x+y, dir_dict[isbad].values())
    if direction in dir_dict[isbad]:
        return dir_dict[isbad][direction]/(1.0*allctr)
    else:
        return 0
        
def predict(isbad,sess,instr,direction):
    return (pSessionBad(sess,isbad)*pInstrumentBad(instr,isbad)*pDirectionBad(direction,isbad)*pBad(isbad))/(pSession(sess)*pInstrument(instr)*pDirection(direction))
    
def main():
    conn=mdb.connect('localhost','test','test','test')
    rs=conn.cursor()
    sql="select instrument, session, is_bad_decision,count(*) ct,direction from trade_data t1 join oanda_transaction_result t2 on t1.transaction_id=t2.transaction_id group by instrument, is_bad_decision, session, direction order by 1,2,3"
    rs.execute(sql)

    for (instr,sess,isbad,ct,direction) in rs:
        sdict=sess_dict[isbad]
        if sess in sdict:
            sdict[sess]=sdict[sess]+ct
        else:
            sdict[sess]=ct
        idict=instr_dict[isbad]
        if instr in idict:
            idict[instr]=idict[instr]+ct
        else:
            idict[instr]=ct
        ddict=dir_dict[isbad]
        if direction in ddict:
            ddict[direction]=ddict[direction]+ct
        else:
            ddict[direction]=ct
            
    conn.close()

if __name__ == '__main__':
    main()
    ##print pSessionBad('MORNING','Y')
    ##print pInstrumentBad('AUD_HKD','Y')
    ##print pBad('Y')
    ##print pSession('MORNING')
    ##print pInstrument('AUD_HKD')
    ##print predict('Y','MORNING','AUD_HKD')
    print "Bad:" + str(predict(Y,sys.argv[1],sys.argv[2],sys.argv[3]))
    print "Good:" + str(predict(N,sys.argv[1],sys.argv[2],sys.argv[3]))

    ##print pInstrument('AUD_CAD')
    
