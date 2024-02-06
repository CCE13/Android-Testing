package com.mygdx.runai;

import java.util.List;
import java.util.Random;

public class Test {

    public int RandomNumber(int minNumber, List<Integer> weightsInOrder){
        int totalWeight = 0;
        for (int weight : weightsInOrder) {
            totalWeight += weight;
        }

        Random random = new Random();
        int randomWeight = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        int selectedIndex = 0;
        for (int i = 0; i < weightsInOrder.size(); i++) {
            cumulativeWeight += weightsInOrder.get(i);
            if (randomWeight < cumulativeWeight) {
                selectedIndex = i;
                break;
            }
        }

        return minNumber + selectedIndex + 1;
    }
}
