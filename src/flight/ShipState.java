package Ship;

public enum ShipState
{
    WAITING, // when a ship procedure is waiting on some conditional
    PROCESSING, // when a ship procedure is currently operating and nothing can be done
    PLAN_FAULT, // a ship is unable to progress in its procedure due to strict lack of requirements
    INACTIVE // when a ship has finished its action queue
}
