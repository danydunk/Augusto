package petclinic.owners;

import java.util.*;
import javax.inject.*;
import javax.transaction.*;

import petclinic.util.*;

/**
 * A domain service class for {@link Owner}-related business operations.
 */
@Transactional
public class OwnerMaintenance
{
   @Inject private Database db;

   public Owner findById(int ownerId)
   {
      return db.findById(Owner.class, ownerId);
   }

   /**
    * Finds the owners whose last name <em>starts</em> with the given name.
    *
    * @param lastName a prefix of the owner's last name to search for
    *
    * @return list of matching owners (empty if none found)
    */
   public List<Owner> findByLastName(String lastName)
   {
      return db.find("select o from Owner o where o.lastName like ?1", lastName + '%');
   }

   public void createOrUpdate(Owner newData)
   {
      db.save(newData);
   }
}
