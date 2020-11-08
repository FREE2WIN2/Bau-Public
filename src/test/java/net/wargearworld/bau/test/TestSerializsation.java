package net.wargearworld.bau.test;

import net.wargearworld.bau.tools.cannon_timer.CannonTimer;
import net.wargearworld.bau.tools.cannon_timer.CannonTimerBlock;
import net.wargearworld.bau.utils.Loc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestSerializsation {

    private CannonTimer cannonTimer;

    @BeforeEach
    public void init() {
        cannonTimer = new CannonTimer();
        Loc loc = new Loc(1, 1, 1);
        CannonTimerBlock cannonTimerBlock = new CannonTimerBlock(loc);
        cannonTimerBlock.addTick();
        cannonTimerBlock.getTick(1).setAmount(123);
        cannonTimer.addBlock(loc, new CannonTimerBlock(loc));
    }

    @Test
    public void serializeTest() {
        File outputFile = new File("serialize.ser");
        try {
            outputFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(cannonTimer);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputFile.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(outputFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            CannonTimer cannonTimer = (CannonTimer) objectInputStream.readObject();
            objectInputStream.close();

            assertEquals(cannonTimer,cannonTimer,"NOT equal");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
