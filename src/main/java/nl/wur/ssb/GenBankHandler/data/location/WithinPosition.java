package nl.wur.ssb.GenBankHandler.data.location;

public class WithinPosition implements Position
{
  public int def;
  public int start;
  public int end;
  
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
  
  @Override
  public int getMinPos()
  {
    return this.start;
  }

  @Override
  public int getMaxPos()
  {
    return this.end;
  }
}
