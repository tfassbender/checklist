import { useState, useContext } from 'react'
import { useLists } from '../hooks/useLists'
import ListCard from '../components/ListCard'
import ConfirmDialog from '../components/ConfirmDialog'
import { AuthContext } from '../context/AuthContext'
import * as api from '../services/api'

export default function ListsOverviewPage() {
  const authContext = useContext(AuthContext)
  const username = authContext?.username || 'User'
  const { lists, isLoading, error, createList, deleteList, fetchLists } = useLists()
  const [isCreating, setIsCreating] = useState(false)
  const [newListName, setNewListName] = useState('')
  const [createError, setCreateError] = useState<string | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; name: string } | null>(null)
  const [deactivateTarget, setDeactivateTarget] = useState<{ id: string; name: string } | null>(null)
  const [showSearch, setShowSearch] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')

  // Filter lists by search query
  const filteredLists = searchQuery.trim()
    ? lists.filter(list =>
        list.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : lists

  const activeLists = filteredLists.filter(list => list.active)
  const inactiveLists = filteredLists.filter(list => !list.active)

  // Calculate incomplete active lists (have items but not all are checked)
  const incompleteActiveLists = activeLists.filter(
    list => list.itemCount > 0 && list.checkedCount < list.itemCount
  )

  // Check if all active lists are completed
  const allActiveListsCompleted =
    activeLists.length > 0 &&
    activeLists.every(list => list.itemCount > 0 && list.checkedCount === list.itemCount)

  // Generate status text
  const getStatusText = () => {
    if (lists.length === 0) return 'Create your first checklist'

    const activeCount = activeLists.length
    const inactiveCount = inactiveLists.length
    const incompleteCount = incompleteActiveLists.length

    const parts = []

    if (activeCount > 0) {
      const activeText = `${activeCount} Active ${activeCount === 1 ? 'List' : 'Lists'}`
      if (incompleteCount > 0) {
        const incompleteText = incompleteCount === 1 ? '1 incomplete' : `${incompleteCount} incomplete`
        parts.push(`${activeText} (${incompleteText})`)
      } else {
        parts.push(activeText)
      }
    }

    if (inactiveCount > 0) {
      parts.push(`${inactiveCount} Inactive ${inactiveCount === 1 ? 'List' : 'Lists'}`)
    }

    if (parts.length === 0) return 'No lists'

    const result = parts.join(' and ')

    // Add filter info if searching
    if (searchQuery.trim()) {
      return `${result} (filtered from ${lists.length} total)`
    }

    return result
  }

  const handleCreateList = async (e: React.FormEvent) => {
    e.preventDefault()
    const name = newListName.trim()
    if (!name) return

    setCreateError(null)
    try {
      await createList(name)
      setNewListName('')
      setIsCreating(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create list'
      setCreateError(message)
    }
  }

  const handleDeleteList = async () => {
    if (!deleteTarget) return
    try {
      await deleteList(deleteTarget.id)
      setDeleteTarget(null)
    } catch (err) {
      console.error('Failed to delete list:', err)
    }
  }

  const handleToggleActive = async (id: string) => {
    const list = lists.find(l => l.id === id)
    if (!list) return

    // If list is active, show confirmation before deactivating
    if (list.active) {
      setDeactivateTarget({ id, name: list.name })
      return
    }

    // If inactive, activate immediately without confirmation
    try {
      await api.activateList(id)
      await fetchLists()
    } catch (err) {
      console.error('Failed to activate list:', err)
    }
  }

  const handleDeactivateList = async () => {
    if (!deactivateTarget) return
    try {
      await api.deactivateList(deactivateTarget.id)
      await fetchLists()
      setDeactivateTarget(null)
    } catch (err) {
      console.error('Failed to deactivate list:', err)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <div className="card p-6 text-center">
          <p className="text-red-600 dark:text-red-400 mb-4">{error}</p>
          <button onClick={fetchLists} className="btn btn-primary">
            Try Again
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto p-4 pb-24">
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            {username.endsWith('s') ? `${username}'` : `${username}'s`} Lists
          </h1>
          <button
            onClick={() => {
              setShowSearch(!showSearch)
              if (showSearch) {
                setSearchQuery('')
              }
            }}
            className="btn-icon"
            title={showSearch ? 'Hide search' : 'Search lists'}
          >
            <svg
              className="w-5 h-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
          </button>
        </div>

        {showSearch && (
          <div className="mb-4">
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Search lists..."
              className="input w-full"
              autoFocus
            />
          </div>
        )}

        <p className="text-gray-600 dark:text-gray-400 mt-1">
          {getStatusText()}
        </p>
        {allActiveListsCompleted && (
          <div className="flex items-center gap-2 mt-2 text-green-600 dark:text-green-400">
            <svg
              className="w-5 h-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span className="text-sm font-medium">All active lists completed</span>
          </div>
        )}
      </div>

      {isCreating && (
        <div className="card p-4 mb-4">
          <form onSubmit={handleCreateList}>
            {createError && (
              <div className="mb-3 p-3 rounded-lg bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 text-sm">
                {createError}
              </div>
            )}
            <input
              type="text"
              value={newListName}
              onChange={e => setNewListName(e.target.value)}
              placeholder="List name"
              className="input mb-3"
              autoFocus
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setIsCreating(false)
                  setNewListName('')
                  setCreateError(null)
                }}
                className="btn btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={!newListName.trim()}
                className="btn btn-primary"
              >
                Create
              </button>
            </div>
          </form>
        </div>
      )}

      {lists.length === 0 && !isCreating ? (
        <div className="card p-8 text-center">
          <svg
            className="w-16 h-16 mx-auto mb-4 text-gray-400 dark:text-gray-500"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M9 11l3 3L22 4" />
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
          </svg>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No lists yet
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            Create your first checklist to get started
          </p>
          <button onClick={() => setIsCreating(true)} className="btn btn-primary">
            Create List
          </button>
        </div>
      ) : filteredLists.length === 0 && searchQuery.trim() ? (
        <div className="card p-8 text-center">
          <svg
            className="w-16 h-16 mx-auto mb-4 text-gray-400 dark:text-gray-500"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No lists found
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            No lists match "{searchQuery}"
          </p>
          <button
            onClick={() => setSearchQuery('')}
            className="btn btn-secondary"
          >
            Clear Search
          </button>
        </div>
      ) : (
        <>
          {/* Active Lists Section */}
          {activeLists.length > 0 && (
            <div className="mb-8">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
                Active Lists
              </h2>
              <div className="grid gap-4 sm:grid-cols-2">
                {activeLists.map(list => (
                  <ListCard
                    key={list.id}
                    list={list}
                    onDelete={id => {
                      const target = lists.find(l => l.id === id)
                      if (target) {
                        setDeleteTarget({ id, name: target.name })
                      }
                    }}
                    onToggleActive={handleToggleActive}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Inactive Lists Section */}
          {inactiveLists.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
                Inactive Lists
              </h2>
              <div className="grid gap-4 sm:grid-cols-2">
                {inactiveLists.map(list => (
                  <ListCard
                    key={list.id}
                    list={list}
                    onDelete={id => {
                      const target = lists.find(l => l.id === id)
                      if (target) {
                        setDeleteTarget({ id, name: target.name })
                      }
                    }}
                    onToggleActive={handleToggleActive}
                  />
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {!isCreating && lists.length > 0 && (
        <button
          onClick={() => setIsCreating(true)}
          className="fixed bottom-6 right-6 w-14 h-14 rounded-full bg-primary-600 text-white
                     shadow-lg hover:bg-primary-700 focus:outline-none focus:ring-2
                     focus:ring-primary-500 focus:ring-offset-2 flex items-center justify-center"
          title="Create new list"
        >
          <svg
            className="w-6 h-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        title="Delete List"
        message={`Are you sure you want to delete "${deleteTarget?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        confirmVariant="danger"
        onConfirm={handleDeleteList}
        onCancel={() => setDeleteTarget(null)}
      />

      <ConfirmDialog
        isOpen={!!deactivateTarget}
        title="Deactivate List"
        message={`Are you sure you want to deactivate "${deactivateTarget?.name}"? This will uncheck all items in the list.`}
        confirmText="Deactivate"
        onConfirm={handleDeactivateList}
        onCancel={() => setDeactivateTarget(null)}
      />
    </div>
  )
}
