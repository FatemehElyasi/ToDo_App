package ir.fatemehelyasi.todogit.fragments.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import ir.fatemehelyasi.todogit.R
import ir.fatemehelyasi.todogit.data.models.ToDoData
import ir.fatemehelyasi.todogit.data.viewmodel.ToDoViewModel
import ir.fatemehelyasi.todogit.databinding.FragmentListBinding
import ir.fatemehelyasi.todogit.fragments.SharedViewModel
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentListBinding
    private val adapter: MyListAdapter by lazy { MyListAdapter() }


    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(layoutInflater)

        //navigate between fragments
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        //recycler
        setUpRecyclerView()

        //viewModel
        // return list of data from dataclass database
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            //fun recycler
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })
        //empty DB
        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner, Observer {
            showEmptyDatabaseViews(it)
        })

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //----------------------------------------------------------------------------menu
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.list_fragment_menu, menu)
                //search
                val search = menu.findItem(R.id.search)
                val searchView = search.actionView as? SearchView
                searchView?.setOnQueryTextListener(this@ListFragment)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                if (menuItem.itemId == R.id.menu_delete_all) {
                    confirmRemovalToAllData()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    //----------------------------------------------------------------------------setUpRecyclerView
    private fun setUpRecyclerView() {
        val recyclerview = binding.recyclerview
        recyclerview.adapter = adapter
        recyclerview.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        // swipe To Delete
        swipeToDelete(binding.recyclerview)
    }

    //----------------------------------------------------------------------------swipeToDelete
    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                //Delete Item
                mToDoViewModel.deleteData(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                //restore deleted Item
                restoreDeleteData(viewHolder.itemView, deletedItem, viewHolder.adapterPosition)
                //           Toast.makeText(requireContext(),"Successfully Removed:'${deletedItem.title}'",Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    //    ----------------------------------------------------------------------------Undo
    private fun restoreDeleteData(view: View, deletedItem: ToDoData, position: Int) {
        val snackBar = Snackbar.make(
            view, "Deleted ${deletedItem.title} ", Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        // snackBar background color
        // snackBar.setBackgroundTint(resources.getColor(com.google.android.material.R.color.design_default_color_on_surface))
        //  snackBar.setActionTextColor(resources.getColor(R.color.actionTextColor))
        snackBar.show()
    }

    //----------------------------------------------------------------------------//sweet Alert Dialog
    private fun confirmRemovalToAllData() {
        val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)

        dialog.titleText = "Are you sure you want to remove everything?"
        dialog.confirmText = "Delete"
        dialog.cancelText = "Cancel"

        dialog.setCancelClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Canceled to removed everything ", Toast.LENGTH_SHORT)
                .show()
        }

        dialog.setConfirmClickListener {
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(), "Removed everything ", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    //----------------------------------------------------------------------------
    private fun showEmptyDatabaseViews(emptyDatabase: Boolean) {
        if (emptyDatabase) {

            binding.noDataImageView.visibility = View.VISIBLE
            binding.noDataTextView.visibility = View.VISIBLE
        } else {
            binding.noDataImageView.visibility = View.INVISIBLE
            binding.noDataTextView.visibility = View.INVISIBLE
        }
    }

    //----------------------------------------------------------------------------
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { List ->
            List?.let {
                adapter.setData(it)
            }

        })
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

}