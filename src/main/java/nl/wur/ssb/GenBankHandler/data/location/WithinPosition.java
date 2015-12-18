package nl.wur.ssb.GenBankHandler.data.location;

public class WithinPosition implements Position
{
  private int def;
  private int start;
  private int end;
  
  public WithinPosition(int def, int start, int end)
  {
    this.def = def;
    this.start = start;
    this.end = end;
  }
  
  public String toString(int offset)
  {
    // (8.10)
    return "" + (start + offset) + "." + (end + offset);
  }
  
  public int getMinPos()
  {
    return this.start;
  }

  public int getMaxPos()
  {
    return this.end;
  }
}
