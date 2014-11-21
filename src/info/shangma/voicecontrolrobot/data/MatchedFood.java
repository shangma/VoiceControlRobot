/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.shangma.voicecontrolrobot.data;

/**
 * wrapper class to determine match term position
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class MatchedFood
{
    private int firstMatchTermIndex;
    private int lastMatchTermIntex;
    private Food food;

    public MatchedFood(int firstMatchTermIndex, int lastMatchTermIntex,
            Food food)
    {
        this.firstMatchTermIndex = firstMatchTermIndex;
        this.lastMatchTermIntex = lastMatchTermIntex;
        this.food = food;
    }
    public int getFirstMatchTermIndex()
    {
        return firstMatchTermIndex;
    }
    public int getLastMatchTermIntex()
    {
        return lastMatchTermIntex;
    }
    public Food getFood()
    {
        return food;
    }
}
