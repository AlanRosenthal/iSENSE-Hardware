//
//  DataSaver.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSaver.h"

@implementation DataSaver

@synthesize count;

-(id) init {
    self = [super init];
    if (self) {
        dataQueue = [[NSMutableDictionary alloc] init];
        count = 0;
    }
    return self;
}

-(void)addDataSet:(DataSet *)dataSet {
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    count++;
}

-(id)removeDataSet:(int)key {
    count--;
    return [dataQueue removeFromQueueWithKey:key];
}

-(void)editDataSetWithKey:(int)key {
    DataSet *dataSet = [dataQueue removeFromQueueWithKey:key];
    /*
     * Do editing code here!
     */
    [dataQueue enqueue:dataSet withKey:key];
}

-(bool)upload {
    iSENSE *isenseAPI = [iSENSE getInstance];
    if (![isenseAPI isLoggedIn]) {
        NSLog(@"Not logged in.");
        return false;
    }
    DataSet *currentDS;
    while (count) {
        // get the next dataset
        int headKey = dataQueue.allKeys[0];
        currentDS = [self removeDataSet:headKey];
        
        // check if the session is uploadable
        if ([currentDS uploadable]) {
            
            // create a session
            if (currentDS.sid.intValue == -1) {
                int sessionID = [isenseAPI createSession:currentDS.name withDescription:currentDS.description Street:currentDS.address City:currentDS.city Country:currentDS.country toExperiment:[NSNumber numberWithInt:currentDS.eid]];
                if (sessionID == -1) {
                    [self addDataSet:currentDS];
                    continue;
                } else {
                    currentDS.sid = [NSNumber numberWithInt:sessionID];
                }
            }
            
            // Upload to iSENSE (pass me JSON data so we can putSessionData)
            if (((NSArray *)currentDS.data).count) {
                NSError *error = nil;
                NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:currentDS.data options:0 error:&error];
                if (error != nil) {
                    [self addDataSet:currentDS];
                    NSLog(@"%@", error);
                    return false;
                }
                
                if (![isenseAPI putSessionData:dataJSON forSession:[NSNumber numberWithInt:currentDS.sid] inExperiment:[NSNumber numberWithInt:currentDS.eid]]) {
                    [self addDataSet:currentDS];
                    continue;
                }
            }
            
            // Upload pictures to iSENSE
            if (((NSArray *)currentDS.picturePaths).count) {
                NSArray *pictures = (NSArray *) currentDS.picturePaths;
                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
                bool failedAtLeastOnce = false;
                
                // Loop through all the images and try to upload them
                for (int i = 0; i < pictures.count; i++) {
                    
                    // Track the images that fail to upload
                    if (![isenseAPI upload:pictures[i] toExperiment:[NSNumber numberWithInt:currentDS.eid] forSession:[NSNumber numberWithInt:currentDS.sid] withName:currentDS.name andDescription:currentDS.description]) {
                        failedAtLeastOnce = true;
                        [newPicturePaths addObject:pictures[i]];
                        continue;
                    }

                }
                
                // Add back the images that need to be uploaded
                if (failedAtLeastOnce) {
                    currentDS.picturePaths = newPicturePaths;
                    [self addDataSet:currentDS];
                    continue;
                }
            }
            
        } else {
            [self addDataSet:currentDS];
        }
        
        
    }
    
    return true;
}

@end