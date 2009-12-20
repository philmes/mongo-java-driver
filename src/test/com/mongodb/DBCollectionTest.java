/**
 *      Copyright (C) 2008 10gen Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.mongodb.util.TestCase;

public class DBCollectionTest extends TestCase {

    public DBCollectionTest()
        throws IOException , MongoException {
        super();
        _db = new Mongo( "127.0.0.1" ).getDB( "cursortest" );
    }

    /**
     * Test inserting a large collection that requires multiple
     * buffers
     */
    @Test(groups = {"basic"})
    public void testInsertLargeCollection()
    {
    	DBCollection c = _db.getCollection("test");
        c.drop();
        
        String objectString = "abcdefghijklmnopqrstuvwxyz123456";
        objectString += objectString + objectString + objectString + objectString;
        
        List<DBObject> dbObjects = new ArrayList<DBObject>();
        
        for(int x = 0; x < ((Bytes.MAX_OBJECT_SIZE + 10) / objectString.length()); x++)
        {
        	dbObjects.add(BasicDBObjectBuilder.start().add("x", objectString).add("_id", x).get());
        }
        
        c.insert(dbObjects);

        c = _db.getCollection("test");
        
        List<DBObject> found = c.find().toArray(); 
        
        assertEquals(found.size(), dbObjects.size());
        
        for(DBObject o : found)
        {
        	dbObjects.remove(o);
        }
        
        assertEquals(0, dbObjects.size());
    }
    
    @Test(groups = {"basic"})
    public void testFindOne() {
        DBCollection c = _db.getCollection("test");
        c.drop();
        
        DBObject obj = c.findOne();
        assertEquals(obj, null);

        DBObject inserted = BasicDBObjectBuilder.start().add("x",1).add("y",2).get();
        c.insert(inserted);
        c.insert(BasicDBObjectBuilder.start().add("_id", 123).add("x",2).add("z",2).get());

        obj = c.findOne(123);
        assertEquals(obj.get("_id"), 123);
        assertEquals(obj.get("x"), 2);
        assertEquals(obj.get("z"), 2);

        obj = c.findOne(123, new BasicDBObject("x", 1));
        assertEquals(obj.get("_id"), 123);
        assertEquals(obj.get("x"), 2);
        assertEquals(obj.containsField("z"), false);

        obj = c.findOne(new BasicDBObject("x", 1));
        assertEquals(obj.get("x"), 1);
        assertEquals(obj.get("y"), 2);

        obj = c.findOne(new BasicDBObject("x", 1), new BasicDBObject("y", 1));
        assertEquals(obj.containsField("x"), false);
        assertEquals(obj.get("y"), 2);
    }
    
    @Test
    public void testDropIndex(){
        DBCollection c = _db.getCollection( "dropindex1" );
        c.drop();

        c.save( new BasicDBObject( "x" , 1 ) );
        assertEquals( 1 , c.getIndexInfo().size() );

        c.ensureIndex( new BasicDBObject( "x" , 1 ) );
        assertEquals( 2 , c.getIndexInfo().size() );

        c.dropIndexes();
        assertEquals( 1 , c.getIndexInfo().size() );

        c.ensureIndex( new BasicDBObject( "x" , 1 ) );
        assertEquals( 2 , c.getIndexInfo().size() );

        c.ensureIndex( new BasicDBObject( "y" , 1 ) );
        assertEquals( 3 , c.getIndexInfo().size() );
        
        c.dropIndex( new BasicDBObject( "x" , 1 ) );
        assertEquals( 2 , c.getIndexInfo().size() );
        
    }

    @Test
    public void testDistinct(){
        DBCollection c = _db.getCollection( "distinct1" );
        c.drop();

        for ( int i=0; i<100; i++ ){
            BasicDBObject o = new BasicDBObject();
            o.put( "_id" , i );
            o.put( "x" , i % 10 );
            c.save( o );
        }

        List l = c.distinct( "x" );
        assertEquals( 10 , l.size() );

        l = c.distinct( "x" , new BasicDBObject( "_id" , new BasicDBObject( "$gt" , 95 ) ) );
        assertEquals( 4 , l.size() );

    }

    final DB _db;

    public static void main( String args[] )
        throws Exception {
        (new DBCollectionTest()).runConsole();
    }

}
