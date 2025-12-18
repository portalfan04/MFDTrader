package util.save;

import simulation.Universe;
import java.io.*;

public class SaveManager
{
    public static void saveUniverse(Universe uni, String file)
    {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file)))
        {
            out.writeObject(uni);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Universe loadUniverse(String file)
    {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file)))
        {
            return (Universe) in.readObject();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
